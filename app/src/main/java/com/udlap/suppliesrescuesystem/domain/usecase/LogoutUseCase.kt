package com.udlap.suppliesrescuesystem.domain.usecase

import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for logging out a user from the system.
 *
 * @property repository The [AuthRepository] used for authentication operations.
 */
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the logout process.
     */
    operator fun invoke() {
        repository.logout()
    }
}
