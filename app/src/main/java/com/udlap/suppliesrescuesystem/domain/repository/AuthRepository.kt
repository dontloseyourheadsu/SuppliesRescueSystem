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
    suspend fun register(email: String, password: String, role: String, name: String): Result<User>

    /**
     * Authenticates a user with their email and password.
     *
     * @param email User's email address.
     * @param password User's password.
     * @return Result containing the authenticated User object on success, or a failure exception.
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Logs out the currently authenticated user.
     */
    fun logout()

    /**
     * Retrieves the currently logged-in user, if any.
     *
     * @return The current User object or null if no user is logged in.
     */
    fun getCurrentUser(): User?

    /**
     * Fetches a list of all users with the RECIPIENT role.
     *
     * @return Result containing a list of User objects with the RECIPIENT role on success, or a failure exception.
     */
    suspend fun getRecipients(): Result<List<User>>
}
