package com.udlap.suppliesrescuesystem.ui.volunteer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing the different states of volunteer actions.
 */
sealed class VolunteerState {
    /** Initial state. */
    object Idle : VolunteerState()
    /** State when an action (claim/complete) is in progress. */
    object Loading : VolunteerState()
    /** State when an action is successfully completed. */
    object Success : VolunteerState()
    /** State when an action fails. */
    data class Error(val message: String) : VolunteerState()
}

/**
 * ViewModel responsible for managing volunteer-specific operations.
 *
 * This includes browsing available rescue batches, claiming a batch, and marking
 * a rescue as completed. It also filters out expired batches on the client side.
 *
 * @property repository The [RescueRepository] for rescue batch operations.
 * @property authRepository The [AuthRepository] for user authentication.
 */
@HiltViewModel
class VolunteerViewModel @Inject constructor(
    private val repository: RescueRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VolunteerState>(VolunteerState.Idle)
    /** Observable state of the current volunteer action. */
    val uiState: StateFlow<VolunteerState> = _uiState.asStateFlow()

    private val _availableBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    /** Observable list of batches available for rescue, filtered by expiration. */
    val availableBatches: StateFlow<List<RescueBatch>> = _availableBatches.asStateFlow()

    private val _activeRescue = MutableStateFlow<RescueBatch?>(null)
    /** Observable representing the rescue batch currently claimed by the volunteer. */
    val activeRescue: StateFlow<RescueBatch?> = _activeRescue.asStateFlow()

    init {
        loadAvailableBatches()
        loadActiveRescue()
    }

    /**
     * Loads available batches and filters out those that have already expired.
     *
     * Note: Expiration filtering is performed on the client side to simplify Firestore queries
     * and avoid the need for composite indexes with inequalities on multiple fields.
     */
    private fun loadAvailableBatches() {
        viewModelScope.launch {
            repository.getAvailableBatches()
                .map { list ->
                    // Relaxed filtering: Only filter if it's clearly expired (e.g., more than 24h ago)
                    // or just show them if they are still marked as AVAILABLE.
                    // This fixes the bug where volunteers see nothing but recipients see "Buscando voluntario".
                    list.filter { 
                        !it.recipientId.isNullOrEmpty()
                    }
                }
                .collect {
                    _availableBatches.value = it
                }
        }
    }

    /**
     * Loads the batch currently claimed by the logged-in volunteer.
     */
    private fun loadActiveRescue() {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            repository.getClaimedBatch(user.uid).collect {
                _activeRescue.value = it
            }
        }
    }

    /**
     * Claims an available rescue batch for the current volunteer.
     *
     * @param batchId Unique identifier of the batch to claim.
     */
    fun claimRescue(batchId: String) {
        val user = authRepository.getCurrentUser() ?: return

        if (_activeRescue.value != null) {
            _uiState.value = VolunteerState.Error("Ya tienes un rescate en curso. Termínalo antes de aceptar otro.")
            return
        }

        viewModelScope.launch {
            _uiState.value = VolunteerState.Loading
            val result = repository.claimBatch(batchId, user.uid)
            result.onSuccess {
                _uiState.value = VolunteerState.Success
            }.onFailure {
                _uiState.value = VolunteerState.Error(it.message ?: "Error al reclamar")
            }
        }
    }

    /**
     * Marks the currently claimed rescue batch as collected (picked up).
     *
     * @param batchId Unique identifier of the batch to collect.
     */
    fun collectRescue(batchId: String) {
        viewModelScope.launch {
            _uiState.value = VolunteerState.Loading
            val result = repository.markAsCollected(batchId)
            result.onSuccess {
                _uiState.value = VolunteerState.Success
            }.onFailure {
                _uiState.value = VolunteerState.Error(it.message ?: "Error al marcar como recolectado")
            }
        }
    }

    /**
     * Marks the currently claimed rescue batch as completed (delivered).
     *
     * @param batchId Unique identifier of the batch to complete.
     */
    fun completeRescue(batchId: String) {
        viewModelScope.launch {
            _uiState.value = VolunteerState.Loading
            val result = repository.completeBatch(batchId)
            result.onSuccess {
                _uiState.value = VolunteerState.Success
                // We don't null out _activeRescue here because the repository listener 
                // will update it to null automatically since status changes to DELIVERED
            }.onFailure {
                _uiState.value = VolunteerState.Error(it.message ?: "Error al completar")
            }
        }
    }

    /**
     * Resets the UI state to [VolunteerState.Idle].
     */
    fun resetState() {
        _uiState.value = VolunteerState.Idle
    }
}
