package com.udlap.suppliesrescuesystem.domain.repository

import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing food rescue batches and their lifecycle.
 *
 * This repository handles publishing, claiming, completing, and tracking rescue operations.
 */
interface RescueRepository {
    /**
     * Publishes a new food rescue batch to the system.
     *
     * @param batch The RescueBatch entity to be published.
     * @return Result indicating success or failure of the operation.
     */
    suspend fun publishBatch(batch: RescueBatch): Result<Unit>

    /**
     * Retrieves a flow of all rescue batches published by a specific donor.
     *
     * @param donorId Unique identifier of the donor organization.
     * @return Flow containing the list of rescue batches for the donor.
     */
    fun getBatchesByDonor(donorId: String): Flow<List<RescueBatch>>

    /**
     * Retrieves a flow of all rescue batches that are currently available for claiming.
     *
     * @return Flow containing the list of available rescue batches.
     */
    fun getAvailableBatches(): Flow<List<RescueBatch>>

    /**
     * Claims an available rescue batch for a volunteer.
     *
     * @param batchId Unique identifier of the rescue batch.
     * @param volunteerId Unique identifier of the volunteer claiming the batch.
     * @return Result indicating success or failure of the claim operation.
     */
    suspend fun claimBatch(batchId: String, volunteerId: String): Result<Unit>

    /**
     * Marks a rescue batch as delivered by the volunteer.
     *
     * @param batchId Unique identifier of the rescue batch.
     * @return Result indicating success or failure of the completion operation.
     */
    suspend fun completeBatch(batchId: String): Result<Unit>

    /**
     * Retrieves a flow of the rescue batch currently claimed by a volunteer.
     *
     * @param volunteerId Unique identifier of the volunteer.
     * @return Flow containing the currently claimed rescue batch, or null if none is claimed.
     */
    fun getClaimedBatch(volunteerId: String): Flow<RescueBatch?>

    /**
     * Retrieves a flow of rescue batches destined for a specific recipient.
     *
     * @param recipientId Unique identifier of the recipient organization.
     * @return Flow containing the list of rescue batches for the recipient.
     */
    fun getBatchesForRecipient(recipientId: String): Flow<List<RescueBatch>>

    /**
     * Confirms that a rescue batch has been received by the recipient.
     *
     * @param batchId Unique identifier of the rescue batch.
     * @return Result indicating success or failure of the confirmation operation.
     */
    suspend fun confirmReception(batchId: String): Result<Unit>
}
