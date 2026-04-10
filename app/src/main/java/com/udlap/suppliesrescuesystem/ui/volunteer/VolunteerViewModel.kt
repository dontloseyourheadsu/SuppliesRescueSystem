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

sealed class VolunteerState {
    object Idle : VolunteerState()
    object Loading : VolunteerState()
    object Success : VolunteerState()
    data class Error(val message: String) : VolunteerState()
}

@HiltViewModel
class VolunteerViewModel @Inject constructor(
    private val repository: RescueRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VolunteerState>(VolunteerState.Idle)
    val uiState: StateFlow<VolunteerState> = _uiState.asStateFlow()

    private val _availableBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    val availableBatches: StateFlow<List<RescueBatch>> = _availableBatches.asStateFlow()

    private val _activeRescue = MutableStateFlow<RescueBatch?>(null)
    val activeRescue: StateFlow<RescueBatch?> = _activeRescue.asStateFlow()

    init {
        loadAvailableBatches()
        loadActiveRescue()
    }

    private fun loadAvailableBatches() {
        viewModelScope.launch {
            repository.getAvailableBatches()
                .map { list ->
                    // Client-side filtering for expired batches
                    list.filter { it.expiresAt > System.currentTimeMillis() }
                }
                .collect {
                    _availableBatches.value = it
                }
        }
    }

    private fun loadActiveRescue() {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            repository.getClaimedBatch(user.uid).collect {
                _activeRescue.value = it
            }
        }
    }

    fun claimRescue(batchId: String) {
        val user = authRepository.getCurrentUser() ?: return
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

    fun completeRescue(batchId: String) {
        viewModelScope.launch {
            _uiState.value = VolunteerState.Loading
            val result = repository.completeBatch(batchId)
            result.onSuccess {
                _uiState.value = VolunteerState.Success
                _activeRescue.value = null
            }.onFailure {
                _uiState.value = VolunteerState.Error(it.message ?: "Error al completar")
            }
        }
    }

    fun resetState() {
        _uiState.value = VolunteerState.Idle
    }
}
