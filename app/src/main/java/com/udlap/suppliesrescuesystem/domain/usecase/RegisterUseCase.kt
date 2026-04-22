package com.udlap.suppliesrescuesystem.domain.usecase

import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for registering a new user in the system.
 *
 * This class encapsulates the logic for creating a new user account with a specific role
 * and profile information by interacting with the [AuthRepository].
 *
 * @property repository The [AuthRepository] used for registration operations.
 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the registration process for a new user.
     *
     * @param email User's email address.
     * @param password User's chosen password.
     * @param role User's role (e.g., DONOR, VOLUNTEER, RECIPIENT).
     * @param name User's organization or individual name.
     * @return Result containing the created [User] object on success, or a failure exception.
     */
    suspend operator fun invoke(email: String, password: String, role: String, name: String, address: String, phone: String): Result<User> {
        return repository.register(email, password, role, name, address, phone)
    }
}
