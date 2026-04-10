package com.udlap.suppliesrescuesystem.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import com.udlap.suppliesrescuesystem.domain.repository.RescueRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RescueRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : RescueRepository {

    override suspend fun publishBatch(batch: RescueBatch, imageUri: Uri?): Result<Unit> {
        return try {
            var imageUrl: String? = null
            if (imageUri != null) {
                val ref = storage.reference.child("rescue_batches/${batch.id}.jpg")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }
            
            val updatedBatch = batch.copy(imageUrl = imageUrl)
            firestore.collection("rescue_batches").document(updatedBatch.id).set(updatedBatch).await()
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

    override suspend fun getAvailableBatches(): Flow<List<RescueBatch>> = callbackFlow {
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
}
