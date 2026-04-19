package com.udlap.suppliesrescuesystem.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.udlap.suppliesrescuesystem.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserDataStore @Inject constructor(private val context: Context) {

    private object Keys {
        val UID = stringPreferencesKey("user_uid")
        val NAME = stringPreferencesKey("user_name")
        val EMAIL = stringPreferencesKey("user_email")
        val ROLE = stringPreferencesKey("user_role")
    }

    suspend fun saveUser(user: User) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.UID] = user.uid
            prefs[Keys.NAME] = user.name
            prefs[Keys.EMAIL] = user.email
            prefs[Keys.ROLE] = user.role
        }
    }

    suspend fun clearUser() {
        context.userPrefsDataStore.edit { it.clear() }
    }

    val cachedUser: Flow<User?> = context.userPrefsDataStore.data.map { prefs ->
        val uid = prefs[Keys.UID] ?: return@map null
        User(
            uid = uid,
            name = prefs[Keys.NAME] ?: "",
            email = prefs[Keys.EMAIL] ?: "",
            role = prefs[Keys.ROLE] ?: ""
        )
    }
}
