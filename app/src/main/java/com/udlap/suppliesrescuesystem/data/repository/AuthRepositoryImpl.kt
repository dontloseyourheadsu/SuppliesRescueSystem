package com.udlap.suppliesrescuesystem.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    override suspend fun register(email: String, password: String, role: String, name: String, address: String, phone: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            
            val user = User(uid = uid, email = email, role = role, name = name, address = address, phone = phone)
            
            // Save user role in Firestore
            firestore.collection("users").document(uid).set(user).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticates a user with their email and password and retrieves their profile from Firestore.
     *
     * @param email User's email address.
     * @param password User's password.
     * @return Result containing the authenticated [User] object on success, or a failure exception.
     */
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            
            val document = firestore.collection("users").document(uid).get().await()
            
            if (!document.exists()) {
                throw Exception("INCOMPLETE_PROFILE")
            }
            
            val user = document.toObject(User::class.java) ?: throw Exception("Failed to parse user")
            
            if (user.role.isBlank() || user.address.isNullOrBlank()) {
                throw Exception("INCOMPLETE_PROFILE")
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticates a user with a Google ID Token.
     *
     * @param idToken The Google ID Token obtained from Credential Manager.
     * @return Result containing the authenticated [User] object on success, or a failure exception.
     */
    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: throw Exception("Google sign-in failed: User is null")
            
            val document = firestore.collection("users").document(firebaseUser.uid).get().await()
            
            if (!document.exists()) {
                throw Exception("INCOMPLETE_PROFILE")
            }

            val user = document.toObject(User::class.java) ?: throw Exception("Failed to parse user")
            
            if (user.role.isBlank() || user.address.isNullOrBlank()) {
                throw Exception("INCOMPLETE_PROFILE")
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches user profile data from Firestore.
     */
    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java) ?: throw Exception("Failed to parse user")
                Result.success(user)
            } else {
                Result.failure(Exception("Profile not found"))
            }
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
        // Note: This User object may be incomplete (role/name/phone missing).
        // It's used as a quick session check.
        return User(uid = firebaseUser.uid, email = firebaseUser.email ?: "")
    }

    /**
     * Fetches all users from Firestore with the 'RECIPIENT' role and joins their needs.
     */
    override suspend fun getRecipientsWithNeeds(): Result<List<User>> {
        return try {
            // 1. Fetch Recipients
            val userSnapshot = firestore.collection("users")
                .whereEqualTo("role", "RECIPIENT")
                .get().await()
            val recipients = userSnapshot.toObjects(User::class.java)

            // 2. Fetch all active needs to join in memory (Spark plan efficient)
            val needsSnapshot = firestore.collection("recipient_needs")
                .get().await()
            val allNeeds = needsSnapshot.toObjects(com.udlap.suppliesrescuesystem.domain.model.RecipientNeed::class.java)

            val recipientsWithNeeds = recipients.map { recipient ->
                val recipientNeeds = allNeeds
                    .filter { it.recipientId == recipient.uid }
                    .map { it.description }
                recipient.copy(needs = recipientNeeds)
            }

            Result.success(recipientsWithNeeds.filter { it.name.isNotBlank() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
