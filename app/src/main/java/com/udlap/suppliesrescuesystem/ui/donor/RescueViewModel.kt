package com.udlap.suppliesrescuesystem.ui.donor

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

sealed class PublishState {
    object Idle : PublishState()
    object Loading : PublishState()
    object Success : PublishState()
    data class Error(val message: String) : PublishState()
}

@HiltViewModel
class RescueViewModel @Inject constructor(
    private val repository: RescueRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _publishState = MutableStateFlow<PublishState>(PublishState.Idle)
    val publishState: StateFlow<PublishState> = _publishState.asStateFlow()

    private val _myBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    val myBatches: StateFlow<List<RescueBatch>> = _myBatches.asStateFlow()

    init {
        loadMyBatches()
    }

    private fun loadMyBatches() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            viewModelScope.launch {
                repository.getBatchesByDonor(user.uid).collect {
                    _myBatches.value = it
                }
            }
        }
    }

    fun publishBatch(title: String, quantity: String, pickupWindow: String) {
        publishBatchExtended(title, quantity, pickupWindow, "", "", "", "")
    }

    fun publishBatchExtended(
        title: String,
        quantity: String,
        pickupWindow: String,
        donorName: String,
        donorAddress: String,
        recipientName: String,
        recipientAddress: String
    ) {
        val user = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            _publishState.value = PublishState.Loading
            val batch = RescueBatch(
                donorId = user.uid,
                donorName = donorName,
                donorAddress = donorAddress,
                recipientName = recipientName,
                recipientAddress = recipientAddress,
                title = title,
                quantity = quantity,
                pickupWindow = pickupWindow,
                expiresAt = System.currentTimeMillis() + (4 * 60 * 60 * 1000) // 4 hours default for MVP
            )
            val result = repository.publishBatch(batch)
            result.onSuccess {
                _publishState.value = PublishState.Success
            }.onFailure {
                _publishState.value = PublishState.Error(it.message ?: "Error publishing")
            }
        }
    }

    fun resetPublishState() {
        _publishState.value = PublishState.Idle
    }
}
