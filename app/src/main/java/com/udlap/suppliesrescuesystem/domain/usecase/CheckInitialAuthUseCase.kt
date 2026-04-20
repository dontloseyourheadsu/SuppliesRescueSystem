package com.udlap.suppliesrescuesystem.domain.usecase

import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case to check for an existing session on app startup.
 *
 * It retrieves the currently authenticated user and fetches their full profile.
 *
 * @property repository The [AuthRepository] used for authentication operations.
 */
class CheckInitialAuthUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the initial authentication check.
     *
     * @return Result containing the User profile on success, or a failure if no user is found.
     */
    suspend operator fun invoke(): Result<User> {
        val currentUser = repository.getCurrentUser()
        return if (currentUser != null) {
            val result = repository.getUserProfile(currentUser.uid)
            result.onSuccess { user ->
                if (user.role.isBlank() || user.address.isNullOrBlank()) {
                    return Result.failure(Exception("INCOMPLETE_PROFILE"))
                }
            }
            result
        } else {
            Result.failure(Exception("No active session found"))
        }
    }
}
