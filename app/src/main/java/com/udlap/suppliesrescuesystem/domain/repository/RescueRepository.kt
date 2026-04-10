package com.udlap.suppliesrescuesystem.domain.repository

import android.net.Uri
import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import kotlinx.coroutines.flow.Flow

interface RescueRepository {
    suspend fun publishBatch(batch: RescueBatch, imageUri: Uri?): Result<Unit>
    fun getBatchesByDonor(donorId: String): Flow<List<RescueBatch>>
    suspend fun getAvailableBatches(): Flow<List<RescueBatch>>
}
