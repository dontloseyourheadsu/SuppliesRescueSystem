package com.udlap.suppliesrescuesystem.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun register(email: String, password: String, role: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            
            val user = User(uid = uid, email = email, role = role)
            
            // Save user role in Firestore
            firestore.collection("users").document(uid).set(user).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            
            // Get user role from Firestore
            val document = firestore.collection("users").document(uid).get().await()
            val role = document.getString("role") ?: "VOLUNTEER" // Default or throw
            
            val user = User(uid = uid, email = email, role = role)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        // Note: For a real app, you might want to fetch the role from a local cache or shared prefs
        // since Firestore is async. For MVP, we can assume role from some state.
        return User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", role = "")
    }
}
