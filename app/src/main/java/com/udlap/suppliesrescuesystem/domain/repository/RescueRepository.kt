package com.udlap.suppliesrescuesystem.domain.repository

import com.udlap.suppliesrescuesystem.domain.model.RescueBatch
import kotlinx.coroutines.flow.Flow

interface RescueRepository {
    suspend fun publishBatch(batch: RescueBatch): Result<Unit>
    fun getBatchesByDonor(donorId: String): Flow<List<RescueBatch>>
    fun getAvailableBatches(): Flow<List<RescueBatch>>
    suspend fun claimBatch(batchId: String, volunteerId: String): Result<Unit>
    suspend fun completeBatch(batchId: String): Result<Unit>
    fun getClaimedBatch(volunteerId: String): Flow<RescueBatch?>
}
