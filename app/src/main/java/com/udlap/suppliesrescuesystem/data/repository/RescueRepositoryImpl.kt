package com.udlap.suppliesrescuesystem.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementation of [RescueRepository] using Cloud Firestore.
 *
 * This repository manages the lifecycle of food rescue batches, from publication by donors
 * to collection by volunteers and delivery to recipients. It uses Firestore snapshots
 * for real-time updates and transactions for safe claiming.
 *
 * @property firestore The [FirebaseFirestore] instance used for database operations.
 */
class RescueRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RescueRepository {

    /**
     * Persists a new food rescue batch in the 'rescue_batches' collection.
     *
     * @param batch The [RescueBatch] to be saved.
     * @return Result indicating success or failure of the write operation.
     */
    override suspend fun publishBatch(batch: RescueBatch): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batch.id).set(batch).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes real-time changes to batches published by a specific donor.
     *
     * @param donorId Unique identifier of the donor.
     * @return Flow of lists containing the donor's rescue batches, ordered by creation date.
     */
    override fun getBatchesByDonor(donorId: String): Flow<List<RescueBatch>> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("donorId", donorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(RescueBatch::class.java) ?: emptyList()
                trySend(batches)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Observes real-time changes to all batches currently marked as 'AVAILABLE'.
     *
     * @return Flow of lists containing available rescue batches.
     */
    override fun getAvailableBatches(): Flow<List<RescueBatch>> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("status", "AVAILABLE")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(RescueBatch::class.java) ?: emptyList()
                trySend(batches)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Claims a batch for a volunteer using a Firestore transaction to prevent double-claiming.
     *
     * This method ensures that only one volunteer can claim an 'AVAILABLE' batch. If the
     * status is no longer 'AVAILABLE' when the transaction executes, the operation fails.
     *
     * @param batchId Unique identifier of the batch.
     * @param volunteerId Unique identifier of the volunteer.
     * @return Result indicating success or failure of the claim transaction.
     */
    override suspend fun claimBatch(batchId: String, volunteerId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val ref = firestore.collection("rescue_batches").document(batchId)
                val snapshot = transaction.get(ref)
                val status = snapshot.getString("status")
                
                if (status == "AVAILABLE") {
                    transaction.update(ref, mapOf(
                        "status" to "CLAIMED",
                        "volunteerId" to volunteerId
                    ))
                } else {
                    throw Exception("Este lote ya ha sido reclamado")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the status of a batch to 'DELIVERED'.
     *
     * @param batchId Unique identifier of the batch.
     * @return Result indicating success or failure of the update operation.
     */
    override suspend fun completeBatch(batchId: String): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batchId)
                .update("status", "DELIVERED").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes the single batch currently claimed by a volunteer.
     *
     * @param volunteerId Unique identifier of the volunteer.
     * @return Flow containing the claimed batch or null if none is claimed.
     */
    override fun getClaimedBatch(volunteerId: String): Flow<RescueBatch?> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("volunteerId", volunteerId)
            .whereEqualTo("status", "CLAIMED")
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val batch = snapshot?.documents?.firstOrNull()?.toObject(RescueBatch::class.java)
                trySend(batch)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Observes batches assigned to a specific recipient that are 'CLAIMED', 'DELIVERED', or 'RECEIVED'.
     *
     * @param recipientId Unique identifier of the recipient organization.
     * @return Flow of lists containing rescue batches for the recipient.
     */
    override fun getBatchesForRecipient(recipientId: String): Flow<List<RescueBatch>> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("recipientId", recipientId)
            .whereIn("status", listOf("CLAIMED", "DELIVERED", "RECEIVED"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(RescueBatch::class.java) ?: emptyList()
                trySend(batches)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Updates the status of a batch to 'RECEIVED' upon recipient confirmation.
     *
     * @param batchId Unique identifier of the batch.
     * @return Result indicating success or failure of the update operation.
     */
    override suspend fun confirmReception(batchId: String): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batchId)
                .update("status", "RECEIVED").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a rescue batch from Firestore.
     *
     * @param batchId Unique identifier of the batch.
     * @return Result indicating success or failure of the delete operation.
     */
    override suspend fun deleteBatch(batchId: String): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batchId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
