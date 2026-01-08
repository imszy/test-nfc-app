package com.nfctools.reader.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nfctools.reader.domain.model.Encoding
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

data class UserPreferences(
    val autoRead: Boolean = true,
    val vibrationFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val defaultEncoding: Encoding = Encoding.UTF_8,
    val historyRetentionDays: Int = 30
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private object PreferencesKeys {
        val AUTO_READ = booleanPreferencesKey("auto_read")
        val VIBRATION_FEEDBACK = booleanPreferencesKey("vibration_feedback")
        val SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback")
        val DEFAULT_ENCODING = stringPreferencesKey("default_encoding")
        val HISTORY_RETENTION_DAYS = intPreferencesKey("history_retention_days")
    }
    
    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            autoRead = prefs[PreferencesKeys.AUTO_READ] ?: true,
            vibrationFeedback = prefs[PreferencesKeys.VIBRATION_FEEDBACK] ?: true,
            soundFeedback = prefs[PreferencesKeys.SOUND_FEEDBACK] ?: false,
            defaultEncoding = try {
                Encoding.valueOf(prefs[PreferencesKeys.DEFAULT_ENCODING] ?: Encoding.UTF_8.name)
            } catch (e: Exception) {
                Encoding.UTF_8
            },
            historyRetentionDays = prefs[PreferencesKeys.HISTORY_RETENTION_DAYS] ?: 30
        )
    }
    
    suspend fun setAutoRead(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_READ] = enabled
        }
    }
    
    suspend fun setVibrationFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.VIBRATION_FEEDBACK] = enabled
        }
    }
    
    suspend fun setSoundFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SOUND_FEEDBACK] = enabled
        }
    }
    
    suspend fun setDefaultEncoding(encoding: Encoding) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DEFAULT_ENCODING] = encoding.name
        }
    }
    
    suspend fun setHistoryRetentionDays(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HISTORY_RETENTION_DAYS] = days
        }
    }
}
