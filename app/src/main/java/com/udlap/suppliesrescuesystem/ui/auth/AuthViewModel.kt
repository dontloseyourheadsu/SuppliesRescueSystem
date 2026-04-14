package com.udlap.suppliesrescuesystem.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.usecase.LoginUseCase
import com.udlap.suppliesrescuesystem.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing the different states of the authentication process.
 */
sealed class AuthState {
    /** Initial state when no authentication action has been taken. */
    object Idle : AuthState()
    /** State when an authentication request (login/register) is in progress. */
    object Loading : AuthState()
    /** State when authentication is successful, containing the [User] profile. */
    data class Success(val user: User) : AuthState()
    /** State when authentication fails, containing an error message. */
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel responsible for handling user authentication flows (Login and Registration).
 *
 * It communicates with [LoginUseCase] and [RegisterUseCase] to perform authentication
 * and exposes the current [AuthState] via a [StateFlow].
 *
 * @property loginUseCase Use case for user login.
 * @property registerUseCase Use case for user registration.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    /**
     * Observable state flow representing the current authentication status.
     */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Initiates the login process with the provided credentials.
     *
     * @param email User's email.
     * @param password User's password.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = loginUseCase(email, password)
            result.onSuccess {
                _authState.value = AuthState.Success(it)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }

    /**
     * Initiates the registration process for a new user.
     *
     * @param email User's email.
     * @param password User's password.
     * @param role Chosen role (DONOR, VOLUNTEER, RECIPIENT).
     * @param name Name of the organization or individual.
     */
    fun register(email: String, password: String, role: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = registerUseCase(email, password, role, name)
            result.onSuccess {
                _authState.value = AuthState.Success(it)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }

    /**
     * Resets the authentication state to [AuthState.Idle].
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
