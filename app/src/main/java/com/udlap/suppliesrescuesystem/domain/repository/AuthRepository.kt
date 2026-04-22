package com.udlap.suppliesrescuesystem.domain.repository

import com.udlap.suppliesrescuesystem.domain.model.User

/**
 * Repository interface for authentication-related operations.
 *
 * This repository manages user registration, login, and retrieval of user information.
 */
interface AuthRepository {
    /**
     * Registers a new user with the specified credentials and role.
     *
     * @param email User's email address.
     * @param password User's chosen password.
     * @param role User's role (e.g., DONOR, VOLUNTEER, RECIPIENT).
     * @param name User's organization or individual name.
     * @return Result containing the created User object on success, or a failure exception.
     */
    suspend fun register(email: String, password: String, role: String, name: String, address: String, phone: String): Result<User>

    /**
     * Authenticates a user with their email and password.
     *
     * @param email User's email address.
     * @param password User's password.
     * @return Result containing the authenticated User object on success, or a failure exception.
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Authenticates a user with a Google ID Token.
     *
     * @param idToken The Google ID Token obtained from Credential Manager.
     * @return Result containing the authenticated User object on success, or a failure exception.
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Logs out the currently authenticated user.
     */
    fun logout()

    /**
     * Retrieves the profile document for the specified user from Firestore.
     *
     * @param uid The unique identifier of the user.
     * @return Result containing the User object on success, or a failure exception.
     */
    suspend fun getUserProfile(uid: String): Result<User>

    /**
     * Retrieves the currently logged-in user, if any.
     *
     * @return The current User object or null if no user is logged in.
     */
    fun getCurrentUser(): User?

    /**
     * Fetches a list of all users with the RECIPIENT role, including their active needs.
     *
     * @return Result containing a list of User objects with the RECIPIENT role on success, or a failure exception.
     */
    suspend fun getRecipientsWithNeeds(): Result<List<User>>
}
