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

sealed class RecipientState {
    object Idle : RecipientState()
    object Loading : RecipientState()
    object Success : RecipientState()
    data class Error(val message: String) : RecipientState()
}

@HiltViewModel
class RecipientViewModel @Inject constructor(
    private val repository: RescueRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipientState>(RecipientState.Idle)
    val uiState: StateFlow<RecipientState> = _uiState.asStateFlow()

    private val _incomingBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    val incomingBatches: StateFlow<List<RescueBatch>> = _incomingBatches.asStateFlow()

    init {
        loadIncomingBatches()
    }

    private fun loadIncomingBatches() {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            repository.getBatchesForRecipient(user.uid).collect {
                _incomingBatches.value = it
            }
        }
    }

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

    fun resetState() {
        _uiState.value = RecipientState.Idle
    }
}
