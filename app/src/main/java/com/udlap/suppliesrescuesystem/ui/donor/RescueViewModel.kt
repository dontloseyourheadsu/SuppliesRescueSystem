package com.udlap.suppliesrescuesystem.ui.donor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udlap.suppliesrescuesystem.data.local.DraftDataStore
import com.udlap.suppliesrescuesystem.domain.model.BatchDraft
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
 */
@HiltViewModel
class RescueViewModel @Inject constructor(
    private val repository: RescueRepository,
    private val authRepository: AuthRepository,
    private val draftDataStore: DraftDataStore
) : ViewModel() {

    private val _publishState = MutableStateFlow<PublishState>(PublishState.Idle)
    /** Observable state of the current publication process. */
    val publishState: StateFlow<PublishState> = _publishState.asStateFlow()

    private val _myBatches = MutableStateFlow<List<RescueBatch>>(emptyList())
    /** Observable list of rescue batches published by the current donor. */
    val myBatches: StateFlow<List<RescueBatch>> = _myBatches.asStateFlow()

    private val _recipients = MutableStateFlow<List<com.udlap.suppliesrescuesystem.domain.model.User>>(emptyList())
    /** Cached list of recipients with their needs. */
    val recipients: StateFlow<List<com.udlap.suppliesrescuesystem.domain.model.User>> = _recipients.asStateFlow()

    private val _orderedRecipients = MutableStateFlow<List<com.udlap.suppliesrescuesystem.domain.model.User>>(emptyList())
    /** Observable list of recipients ordered by relevance to the donation title. */
    val orderedRecipients: StateFlow<List<com.udlap.suppliesrescuesystem.domain.model.User>> = _orderedRecipients.asStateFlow()

    private val _activeNeeds = MutableStateFlow<List<com.udlap.suppliesrescuesystem.domain.model.RecipientNeed>>(emptyList())
    /** Observable list of active needs from recipients. */
    val activeNeeds: StateFlow<List<com.udlap.suppliesrescuesystem.domain.model.RecipientNeed>> = _activeNeeds.asStateFlow()

    /** Observable flow of the current batch publication draft. */
    val batchDraft: StateFlow<BatchDraft> = draftDataStore.batchDraft
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BatchDraft()
        )

    init {
        loadMyBatches()
        loadRecipients()
        loadActiveNeeds()
    }

    private fun loadActiveNeeds() {
        viewModelScope.launch {
            repository.getActiveNeeds().collect {
                _activeNeeds.value = it
            }
        }
    }

    /**
     * Updates the current draft in persistent storage.
     */
    fun updateDraft(draft: BatchDraft) {
        viewModelScope.launch {
            draftDataStore.saveDraft(draft)
        }
    }

    /**
     * Clears the current draft from persistent storage.
     */
    fun clearDraft() {
        viewModelScope.launch {
            draftDataStore.clearDraft()
        }
    }

    /**
     * Fetches the list of all users with the RECIPIENT role and their needs.
     */
    private fun loadRecipients() {
        viewModelScope.launch {
            val result = authRepository.getRecipientsWithNeeds()
            result.onSuccess { 
                _recipients.value = it
                updateOrdering("") // Initial order
            }
        }
    }

    /**
     * Re-orders the recipient list based on the donation title.
     */
    fun updateOrdering(query: String) {
        val list = _recipients.value
        if (query.isBlank()) {
            _orderedRecipients.value = list
            return
        }

        val queryWords = query.lowercase().split(" ").filter { it.isNotBlank() }
        
        val scoredList = list.map { recipient ->
            var score = 0
            recipient.needs.forEach { need ->
                val needLower = need.lowercase()
                queryWords.forEach { word ->
                    if (needLower.contains(word)) score++
                }
            }
            recipient to score
        }

        _orderedRecipients.value = scoredList.sortedByDescending { it.second }.map { it.first }
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
     */
    fun publishBatch(title: String, quantity: String, pickupWindow: String) {
        publishBatchExtended(
            title, quantity, pickupWindow, 
            "", "", "", "", "", 
            System.currentTimeMillis() + (4 * 60 * 60 * 1000)
        )
    }

    /**
     * Publishes a new food rescue batch and clears the draft upon success.
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
                draftDataStore.clearDraft() // Clear draft on success
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
