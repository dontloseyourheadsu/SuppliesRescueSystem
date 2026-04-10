package com.udlap.suppliesrescuesystem.domain.usecase

import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for authenticating a user into the system.
 *
 * This class encapsulates the logic for logging in a user by interacting with the [AuthRepository].
 *
 * @property repository The [AuthRepository] used for authentication operations.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the login process.
     *
     * @param email User's email address.
     * @param password User's password.
     * @return Result containing the authenticated [User] object on success, or a failure exception.
     */
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return repository.login(email, password)
    }
}
