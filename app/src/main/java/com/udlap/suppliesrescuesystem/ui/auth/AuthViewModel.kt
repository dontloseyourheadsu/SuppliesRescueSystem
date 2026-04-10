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

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

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

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
