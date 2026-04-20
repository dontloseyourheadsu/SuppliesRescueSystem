package com.udlap.suppliesrescuesystem.data.local

import android.content.Context
import android.content.SharedPreferences
import com.udlap.suppliesrescuesystem.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataStore @Inject constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _cachedUserFlow = MutableStateFlow<User?>(null)
    val cachedUser: StateFlow<User?> = _cachedUserFlow.asStateFlow()

    private val _rememberMeFlow = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMeFlow.asStateFlow()

    private val _savedEmailFlow = MutableStateFlow<String?>(null)
    val savedEmail: StateFlow<String?> = _savedEmailFlow.asStateFlow()

    init {
        // Synchronous initial load for instant UI
        _cachedUserFlow.value = getSyncUser()
        _rememberMeFlow.value = prefs.getBoolean("remember_me", false)
        _savedEmailFlow.value = prefs.getString("saved_email", null)
    }

    fun saveUser(user: User, rememberMe: Boolean = true) {
        prefs.edit().apply {
            putString("user_uid", user.uid)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putString("user_role", user.role)
            putBoolean("remember_me", rememberMe)
            if (rememberMe) {
                putString("saved_email", user.email)
            }
            apply()
        }
        _cachedUserFlow.value = user
        _rememberMeFlow.value = rememberMe
        if (rememberMe) _savedEmailFlow.value = user.email
    }

    fun setRememberMe(enabled: Boolean) {
        prefs.edit().putBoolean("remember_me", enabled).apply()
        if (!enabled) {
            prefs.edit().remove("saved_email").apply()
            _savedEmailFlow.value = null
        }
        _rememberMeFlow.value = enabled
    }

    fun clearUser() {
        val rememberMeVal = prefs.getBoolean("remember_me", false)
        val savedEmailVal = prefs.getString("saved_email", null)
        
        prefs.edit().clear().apply()
        
        prefs.edit().apply {
            putBoolean("remember_me", rememberMeVal)
            if (rememberMeVal && savedEmailVal != null) {
                putString("saved_email", savedEmailVal)
            }
            apply()
        }
        
        _cachedUserFlow.value = null
        _rememberMeFlow.value = rememberMeVal
        _savedEmailFlow.value = if (rememberMeVal) savedEmailVal else null
    }

    fun getSyncUser(): User? {
        val uid = prefs.getString("user_uid", null) ?: return null
        return User(
            uid = uid,
            name = prefs.getString("user_name", "") ?: "",
            email = prefs.getString("user_email", "") ?: "",
            role = prefs.getString("user_role", "") ?: ""
        )
    }
}
