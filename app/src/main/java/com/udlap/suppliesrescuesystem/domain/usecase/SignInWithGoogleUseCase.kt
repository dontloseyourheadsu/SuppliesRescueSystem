package com.udlap.suppliesrescuesystem.domain.usecase

import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for authenticating a user with Google Sign-In.
 *
 * This class encapsulates the logic for logging in a user using a Google ID Token
 * by interacting with the [AuthRepository].
 *
 * @property repository The [AuthRepository] used for authentication operations.
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the Google Sign-In process.
     *
     * @param idToken The Google ID Token obtained from Credential Manager.
     * @return Result containing the authenticated [User] object on success, or a failure exception.
     */
    suspend operator fun invoke(idToken: String): Result<User> {
        return repository.signInWithGoogle(idToken)
    }
}
