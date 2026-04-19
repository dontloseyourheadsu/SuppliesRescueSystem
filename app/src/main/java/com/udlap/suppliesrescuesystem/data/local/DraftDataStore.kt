package com.udlap.suppliesrescuesystem.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.udlap.suppliesrescuesystem.domain.model.BatchDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "batch_drafts")

/**
 * DataStore manager for batch drafts.
 *
 * Persists temporary batch data for donors during the publication process.
 *
 * @property context The application context.
 */
@Singleton
class DraftDataStore @Inject constructor(private val context: Context) {

    private object Keys {
        val TITLE = stringPreferencesKey("draft_title")
        val QUANTITY = stringPreferencesKey("draft_quantity")
        val PICKUP_WINDOW = stringPreferencesKey("draft_pickup_window")
        val RECIPIENT_ID = stringPreferencesKey("draft_recipient_id")
    }

    /**
     * Saves the current draft state to DataStore.
     */
    suspend fun saveDraft(draft: BatchDraft) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TITLE] = draft.title
            preferences[Keys.QUANTITY] = draft.quantity
            preferences[Keys.PICKUP_WINDOW] = draft.pickupWindow
            preferences[Keys.RECIPIENT_ID] = draft.recipientId
        }
    }

    /**
     * Clears the current draft from DataStore.
     */
    suspend fun clearDraft() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.TITLE)
            preferences.remove(Keys.QUANTITY)
            preferences.remove(Keys.PICKUP_WINDOW)
            preferences.remove(Keys.RECIPIENT_ID)
        }
    }

    /**
     * Retrieves the current draft as a [Flow].
     */
    val batchDraft: Flow<BatchDraft> = context.dataStore.data.map { preferences ->
        BatchDraft(
            title = preferences[Keys.TITLE] ?: "",
            quantity = preferences[Keys.QUANTITY] ?: "",
            pickupWindow = preferences[Keys.PICKUP_WINDOW] ?: "",
            recipientId = preferences[Keys.RECIPIENT_ID] ?: ""
        )
    }
}
