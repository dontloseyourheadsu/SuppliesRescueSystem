package com.udlap.suppliesrescuesystem.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udlap.suppliesrescuesystem.data.local.UserDataStore
import com.udlap.suppliesrescuesystem.domain.model.User
import com.udlap.suppliesrescuesystem.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object NoSession : AuthState()
    object IncompleteProfile : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val checkInitialAuthUseCase: CheckInitialAuthUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val completeProfileUseCase: CompleteProfileUseCase,
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var isLoggingOut = false

    val cachedUser: StateFlow<User?> = userDataStore.cachedUser
    val rememberMe: StateFlow<Boolean> = userDataStore.rememberMe
    val savedEmail: StateFlow<String?> = userDataStore.savedEmail

    init {
        checkSession()
    }

    private fun checkSession() {
        if (isLoggingOut) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // 1. Get from SYNC cache for INSTANT UI
            val cached = userDataStore.getSyncUser()
            if (cached != null) {
                _authState.value = AuthState.Success(cached)
                
                // 2. Refresh in background
                launch {
                    val result = checkInitialAuthUseCase()
                    result.onSuccess { 
                        userDataStore.saveUser(it, rememberMe.value) 
                    }
                }
                return@launch
            }

            // 3. No local profile, check with Firebase
            val result = checkInitialAuthUseCase()
            result.onSuccess {
                userDataStore.saveUser(it, rememberMe.value)
                _authState.value = AuthState.Success(it)
            }.onFailure {
                if (it.message == "INCOMPLETE_PROFILE") {
                    _authState.value = AuthState.IncompleteProfile
                } else {
                    _authState.value = AuthState.NoSession
                }
            }
        }
    }

    fun setRememberMe(enabled: Boolean) {
        viewModelScope.launch {
            userDataStore.setRememberMe(enabled)
        }
    }

    fun login(email: String, password: String) {
        isLoggingOut = false
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = loginUseCase(email, password)
            result.onSuccess {
                userDataStore.saveUser(it, rememberMe.value)
                _authState.value = AuthState.Success(it)
            }.onFailure {
                if (it.message == "INCOMPLETE_PROFILE") {
                    _authState.value = AuthState.IncompleteProfile
                } else {
                    _authState.value = AuthState.Error(it.message ?: "Unknown error")
                }
            }
        }
    }

    fun register(email: String, password: String, role: String, name: String, address: String, phone: String) {
        isLoggingOut = false
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = registerUseCase(email, password, role, name, address, phone)
            result.onSuccess {
                userDataStore.saveUser(it, rememberMe.value)
                _authState.value = AuthState.Success(it)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun completeProfile(name: String, role: String, address: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = completeProfileUseCase(name, role, address, phone)
            result.onSuccess {
                userDataStore.saveUser(it, true)
                _authState.value = AuthState.Success(it)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Failed to complete profile")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        isLoggingOut = false
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = signInWithGoogleUseCase(idToken)
            result.onSuccess {
                userDataStore.saveUser(it, true)
                _authState.value = AuthState.Success(it)
            }.onFailure {
                if (it.message == "INCOMPLETE_PROFILE") {
                    _authState.value = AuthState.IncompleteProfile
                } else {
                    _authState.value = AuthState.Error(it.message ?: "Google sign-in failed")
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        isLoggingOut = true
        viewModelScope.launch {
            logoutUseCase()
            userDataStore.clearUser()
            _authState.value = AuthState.NoSession
        }
    }
}
