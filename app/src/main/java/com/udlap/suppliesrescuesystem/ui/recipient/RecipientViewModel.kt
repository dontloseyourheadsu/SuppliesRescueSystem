package com.udlap.suppliesrescuesystem.ui.recipient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.model.RecipientNeed
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
 * Sealed class representing the different states of recipient actions.
 */
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

    private val _myNeeds = MutableStateFlow<List<RecipientNeed>>(emptyList())
    val myNeeds: StateFlow<List<RecipientNeed>> = _myNeeds.asStateFlow()

    private val _openBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    val openBatches: StateFlow<List<RescueBatch>> = _openBatches.asStateFlow()

    init {
        loadIncomingBatches()
        loadMyNeeds()
        loadOpenBatches()
    }

    private fun loadIncomingBatches() {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            repository.getBatchesForRecipient(user.uid).collect {
                _incomingBatches.value = it
            }
        }
    }

    private fun loadMyNeeds() {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            repository.getNeedsByRecipient(user.uid).collect {
                _myNeeds.value = it
            }
        }
    }

    private fun loadOpenBatches() {
        viewModelScope.launch {
            repository.getAvailableBatches()
                .map { list ->
                    // Filter for batches with NO recipient assigned yet
                    list.filter { it.recipientId.isNullOrEmpty() }
                }
                .collect {
                    _openBatches.value = it
                }
        }
    }

    fun publishNeed(description: String) {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            _uiState.value = RecipientState.Loading
            val need = RecipientNeed(
                recipientId = user.uid,
                recipientName = user.name,
                description = description
            )
            val result = repository.publishNeed(need)
            result.onSuccess {
                _uiState.value = RecipientState.Success
            }.onFailure {
                _uiState.value = RecipientState.Error(it.message ?: "Error publishing need")
            }
        }
    }

    fun deleteNeed(needId: String) {
        viewModelScope.launch {
            _uiState.value = RecipientState.Loading
            repository.deleteNeed(needId).onSuccess {
                _uiState.value = RecipientState.Success
            }.onFailure {
                _uiState.value = RecipientState.Error("Error deleting need")
            }
        }
    }

    fun claimOpenBatch(batchId: String) {
        val user = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            _uiState.value = RecipientState.Loading
            val result = repository.claimOpenBatch(
                batchId = batchId,
                recipientId = user.uid,
                recipientName = user.name,
                recipientAddress = user.address ?: ""
            )
            result.onSuccess {
                _uiState.value = RecipientState.Success
            }.onFailure {
                _uiState.value = RecipientState.Error(it.message ?: "Error claiming batch")
            }
        }
    }

    fun confirmReception(batchId: String) {
        viewModelScope.launch {
            _uiState.value = RecipientState.Loading
            repository.confirmReception(batchId).onSuccess {
                _uiState.value = RecipientState.Success
            }.onFailure {
                _uiState.value = RecipientState.Error("Error confirming reception")
            }
        }
    }

    fun deleteBatch(batchId: String) {
        viewModelScope.launch {
            _uiState.value = RecipientState.Loading
            repository.deleteBatch(batchId).onSuccess {
                _uiState.value = RecipientState.Success
            }.onFailure {
                _uiState.value = RecipientState.Error("Error deleting batch")
            }
        }
    }

    fun resetState() {
        _uiState.value = RecipientState.Idle
    }
}
