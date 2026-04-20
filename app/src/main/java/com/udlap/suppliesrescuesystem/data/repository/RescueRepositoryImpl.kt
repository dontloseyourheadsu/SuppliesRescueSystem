package com.udlap.suppliesrescuesystem.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.model.RecipientNeed
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementation of [RescueRepository] using Cloud Firestore.
 */
class RescueRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RescueRepository {

    override suspend fun publishBatch(batch: RescueBatch): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batch.id).set(batch).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getBatchesByDonor(donorId: String): Flow<List<RescueBatch>> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("donorId", donorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    trySend(emptyList()) // Avoid crash on logout
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(RescueBatch::class.java) ?: emptyList()
                trySend(batches)
            }
        awaitClose { listener.remove() }
    }

    override fun getAvailableBatches(): Flow<List<RescueBatch>> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("status", "AVAILABLE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(RescueBatch::class.java) ?: emptyList()
                // Sort manually in Kotlin to avoid requiring a composite index in Firestore
                trySend(batches.sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

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

    override suspend fun completeBatch(batchId: String): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batchId)
                .update("status", "DELIVERED").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getClaimedBatch(volunteerId: String): Flow<RescueBatch?> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("volunteerId", volunteerId)
            .whereEqualTo("status", "CLAIMED")
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    trySend(null)
                    return@addSnapshotListener
                }
                val batch = snapshot?.documents?.firstOrNull()?.toObject(RescueBatch::class.java)
                trySend(batch)
            }
        awaitClose { listener.remove() }
    }

    override fun getBatchesForRecipient(recipientId: String): Flow<List<RescueBatch>> = callbackFlow {
        val listener = firestore.collection("rescue_batches")
            .whereEqualTo("recipientId", recipientId)
            .whereIn("status", listOf("AVAILABLE", "CLAIMED", "DELIVERED", "RECEIVED"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val batches = snapshot?.toObjects(RescueBatch::class.java) ?: emptyList()
                trySend(batches)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun confirmReception(batchId: String): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batchId)
                .update("status", "RECEIVED").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBatch(batchId: String): Result<Unit> {
        return try {
            firestore.collection("rescue_batches").document(batchId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publishNeed(need: RecipientNeed): Result<Unit> {
        return try {
            firestore.collection("recipient_needs").document(need.id).set(need).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveNeeds(): Flow<List<RecipientNeed>> = callbackFlow {
        val listener = firestore.collection("recipient_needs")
            .whereEqualTo("fulfilled", false)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val needs = snapshot?.toObjects(RecipientNeed::class.java) ?: emptyList()
                trySend(needs)
            }
        awaitClose { listener.remove() }
    }

    override fun getNeedsByRecipient(recipientId: String): Flow<List<RecipientNeed>> = callbackFlow {
        val listener = firestore.collection("recipient_needs")
            .whereEqualTo("recipientId", recipientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val needs = snapshot?.toObjects(RecipientNeed::class.java) ?: emptyList()
                trySend(needs.sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

    override suspend fun deleteNeed(needId: String): Result<Unit> {
        return try {
            firestore.collection("recipient_needs").document(needId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun claimOpenBatch(
        batchId: String,
        recipientId: String,
        recipientName: String,
        recipientAddress: String
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val ref = firestore.collection("rescue_batches").document(batchId)
                val snapshot = transaction.get(ref)
                
                // Use field path to be safe with nullable fields in Firestore
                val existingRecipientId = snapshot.getString("recipientId")
                val status = snapshot.getString("status")
                
                if (status != "AVAILABLE") {
                    throw Exception("Este lote no está disponible")
                }

                if (existingRecipientId.isNullOrEmpty()) {
                    transaction.update(ref, mapOf(
                        "recipientId" to recipientId,
                        "recipientName" to recipientName,
                        "recipientAddress" to recipientAddress
                        // Note: We DON'T change status to CLAIMED yet, 
                        // as a volunteer still needs to claim the transport.
                        // Or should we? Business rule says: 
                        // "evitar que dos voluntarios reclamen la misma recolección"
                        // If a recipient claims it, it's still AVAILABLE for a volunteer.
                    ))
                } else {
                    throw Exception("Este lote ya tiene un receptor asignado")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Silently handles common Firestore errors during session changes.
     */
    private fun handleFirestoreError(error: FirebaseFirestoreException) {
        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            Log.d("Firestore", "Access denied (likely due to logout)")
        } else {
            Log.e("Firestore", "Error fetching data: ${error.message}")
        }
    }
}
