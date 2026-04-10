package com.udlap.suppliesrescuesystem.domain.usecase

import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, role: String): Result<User> {
        return repository.register(email, password, role)
    }
}
