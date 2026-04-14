package com.udlap.suppliesrescuesystem.ui.recipient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing the different states of recipient actions.
 */
sealed class RecipientState {
    /** Initial state. */
    object Idle : RecipientState()
    /** State when a confirmation action is in progress. */
    object Loading : RecipientState()
    /** State when the action is successfully completed. */
    object Success : RecipientState()
    /** State when the action fails. */
    data class Error(val message: String) : RecipientState()
}

/**
 * ViewModel responsible for managing recipient-specific operations.
 *
 * This includes viewing incoming rescue batches and confirming their reception.
 *
 * @property repository The [RescueRepository] for rescue batch operations.
 * @property authRepository The [AuthRepository] for user authentication.
 */
@HiltViewModel
class RecipientViewModel @Inject constructor(
    private val repository: RescueRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipientState>(RecipientState.Idle)
    /** Observable state of the current recipient action. */
    val uiState: StateFlow<RecipientState> = _uiState.asStateFlow()

    private val _incomingBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    /** Observable list of rescue batches assigned to the current recipient. */
    val incomingBatches: StateFlow<List<RescueBatch>> = _incomingBatches.asStateFlow()

    init {
        loadIncomingBatches()
    }

    /**
     * Loads all rescue batches assigned to the currently logged-in recipient.
     */
    private fun loadIncomingBatches() {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            repository.getBatchesForRecipient(user.uid).collect {
                _incomingBatches.value = it
            }
        }
    }

    /**
     * Confirms that a rescue batch has been physically received by the recipient organization.
     *
     * @param batchId Unique identifier of the batch to confirm.
     */
    fun confirmReception(batchId: String) {
        viewModelScope.launch {
            _uiState.value = RecipientState.Loading
            val result = repository.confirmReception(batchId)
            result.onSuccess {
                _uiState.value = RecipientState.Success
            }.onFailure {
                _uiState.value = RecipientState.Error(it.message ?: "Error al confirmar")
            }
        }
    }

    /**
     * Deletes a rescue batch from the system.
     *
     * @param batchId Unique identifier of the batch to delete.
     */
    fun deleteBatch(batchId: String) {
        viewModelScope.launch {
            _uiState.value = RecipientState.Loading
            val result = repository.deleteBatch(batchId)
            result.onSuccess {
                _uiState.value = RecipientState.Success
            }.onFailure {
                _uiState.value = RecipientState.Error(it.message ?: "Error al eliminar")
            }
        }
    }

    /**
     * Resets the UI state to [RecipientState.Idle].
     */
    fun resetState() {
        _uiState.value = RecipientState.Idle
    }
}
