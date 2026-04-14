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

/**
 * Sealed class representing the different states of the batch publication process.
 */
sealed class PublishState {
    /** Initial state. */
    object Idle : PublishState()
    /** State when a batch is being uploaded to Firestore. */
    object Loading : PublishState()
    /** State when the batch has been successfully published. */
    object Success : PublishState()
    /** State when publication fails. */
    data class Error(val message: String) : PublishState()
}

/**
 * ViewModel responsible for managing donor-specific operations.
 *
 * This includes publishing new food rescue batches, viewing the donor's batch history,
 * and loading potential recipients (shelters).
 *
 * @property repository The [RescueRepository] for rescue batch operations.
 * @property authRepository The [AuthRepository] for user authentication and recipient lookups.
 */
@HiltViewModel
class RescueViewModel @Inject constructor(
    private val repository: RescueRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _publishState = MutableStateFlow<PublishState>(PublishState.Idle)
    /** Observable state of the current publication process. */
    val publishState: StateFlow<PublishState> = _publishState.asStateFlow()

    private val _myBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    /** Observable list of rescue batches published by the current donor. */
    val myBatches: StateFlow<List<RescueBatch>> = _myBatches.asStateFlow()

    private val _recipients = MutableStateFlow<List<com.udlap.suppliesrescuesystem.domain.model.User>>(emptyList())
    /** Observable list of potential recipient organizations (shelters). */
    val recipients: StateFlow<List<com.udlap.suppliesrescuesystem.domain.model.User>> = _recipients.asStateFlow()

    init {
        loadMyBatches()
        loadRecipients()
    }

    /**
     * Fetches the list of all users with the RECIPIENT role.
     */
    private fun loadRecipients() {
        viewModelScope.launch {
            val result = authRepository.getRecipients()
            result.onSuccess { _recipients.value = it }
        }
    }

    /**
     * Loads all rescue batches published by the currently logged-in donor.
     */
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

    /**
     * Publishes a new food rescue batch with basic information.
     *
     * @param title Brief name for the food donation.
     * @param quantity Amount of food.
     * @param pickupWindow Timeframe for collection.
     */
    fun publishBatch(title: String, quantity: String, pickupWindow: String) {
        publishBatchExtended(
            title, quantity, pickupWindow, 
            "", "", "", "", "", 
            System.currentTimeMillis() + (4 * 60 * 60 * 1000)
        )
    }

    /**
     * Publishes a new food rescue batch with complete donor and recipient details.
     *
     * @param title Brief name for the food donation.
     * @param quantity Amount of food.
     * @param pickupWindow Timeframe for collection.
     * @param donorName Name of the donor organization.
     * @param donorAddress Physical address of the donor.
     * @param recipientId UID of the recipient shelter.
     * @param recipientName Name of the recipient shelter.
     * @param recipientAddress Physical address of the recipient shelter.
     * @param expiresAt Timestamp in milliseconds when the food will no longer be available or safe.
     */
    fun publishBatchExtended(
        title: String,
        quantity: String,
        pickupWindow: String,
        donorName: String,
        donorAddress: String,
        recipientId: String,
        recipientName: String,
        recipientAddress: String,
        expiresAt: Long
    ) {
        val user = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            _publishState.value = PublishState.Loading
            val batch = RescueBatch(
                donorId = user.uid,
                donorName = donorName,
                donorAddress = donorAddress,
                recipientId = recipientId,
                recipientName = recipientName,
                recipientAddress = recipientAddress,
                title = title,
                quantity = quantity,
                pickupWindow = pickupWindow,
                expiresAt = expiresAt
            )
            val result = repository.publishBatch(batch)
            result.onSuccess {
                _publishState.value = PublishState.Success
            }.onFailure {
                _publishState.value = PublishState.Error(it.message ?: "Error publishing")
            }
        }
    }

    /**
     * Resets the publication state to [PublishState.Idle].
     */
    fun resetPublishState() {
        _publishState.value = PublishState.Idle
    }
}
