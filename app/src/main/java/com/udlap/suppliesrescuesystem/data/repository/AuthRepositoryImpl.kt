package com.udlap.suppliesrescuesystem.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementation of [AuthRepository] using Firebase Authentication and Cloud Firestore.
 *
 * This class handles the actual communication with Firebase to register, login, and
 * manage user session state.
 *
 * @property firebaseAuth The [FirebaseAuth] instance for authentication operations.
 * @property firestore The [FirebaseFirestore] instance for storing user profile data.
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    /**
     * Registers a new user with Firebase Auth and creates a profile document in Firestore.
     *
     * @param email User's email address.
     * @param password User's chosen password.
     * @param role User's role (e.g., DONOR, VOLUNTEER, RECIPIENT).
     * @param name User's organization or individual name.
     * @return Result containing the created [User] object on success, or a failure exception.
     */
    override suspend fun register(email: String, password: String, role: String, name: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            
            val user = User(uid = uid, email = email, role = role, name = name)
            
            // Save user role in Firestore
            firestore.collection("users").document(uid).set(user).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticates a user with Firebase Auth and retrieves their profile from Firestore.
     *
     * @param email User's email address.
     * @param password User's password.
     * @return Result containing the authenticated [User] object on success, or a failure exception.
     */
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            
            // Get user role from Firestore
            val document = firestore.collection("users").document(uid).get().await()
            val role = document.getString("role") ?: "VOLUNTEER"
            val name = document.getString("name") ?: ""
            
            val user = User(uid = uid, email = email, role = role, name = name)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out the user from Firebase Authentication.
     */
    override fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Retrieves the current authenticated user from Firebase Auth.
     *
     * Note: This method currently only returns UID and Email as role/name require a Firestore fetch.
     *
     * @return The current [User] or null if no user is signed in.
     */
    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", role = "", name = "")
    }

    /**
     * Fetches all users from Firestore with the 'RECIPIENT' role.
     *
     * @return Result containing a list of recipient [User] objects on success, or a failure exception.
     */
    override suspend fun getRecipients(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "RECIPIENT")
                .get().await()
            val recipients = snapshot.toObjects(User::class.java)
            Result.success(recipients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
