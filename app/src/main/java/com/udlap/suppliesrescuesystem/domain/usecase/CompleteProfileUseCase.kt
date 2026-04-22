package com.udlap.suppliesrescuesystem.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udlap.suppliesrescuesystem.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CompleteProfileUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend operator fun invoke(name: String, role: String, address: String, phone: String): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: throw Exception("No user logged in")
            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                role = role,
                name = name,
                address = address,
                phone = phone
            )
            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
