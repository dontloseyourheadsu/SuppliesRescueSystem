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
                    close(error)
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
                    close(error)
                    return@addSnapshotListener
                }
                val batch = snapshot?.documents?.firstOrNull()?.toObject(RescueBatch::class.java)
                trySend(batch)
            }
        awaitClose { listener.remove() }
    }
}
