package com.udlap.suppliesrescuesystem.domain.repository

import com.udlap.suppliesrescuesystem.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String, role: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    fun logout()
    fun getCurrentUser(): User?
}
