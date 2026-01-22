package com.plexglassplayer.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.plexglassplayer.data.repositories.di.ServerDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerPreferences @Inject constructor(
    @ServerDataStore
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_ACTIVE_SERVER_ID = stringPreferencesKey("active_server_id")
        private val KEY_ACTIVE_SERVER_URL = stringPreferencesKey("active_server_url")
        private val KEY_MUSIC_SECTION_KEY = stringPreferencesKey("music_section_key")
    }

    val activeServerIdFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_SERVER_ID]
    }

    suspend fun setActiveServer(serverId: String, serverUrl: String, musicSectionKey: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_SERVER_ID] = serverId
            prefs[KEY_ACTIVE_SERVER_URL] = serverUrl
            prefs[KEY_MUSIC_SECTION_KEY] = musicSectionKey
        }
    }

    suspend fun getActiveServerUrl(): String? = dataStore.data.first()[KEY_ACTIVE_SERVER_URL]
    suspend fun getMusicSectionKey(): String? = dataStore.data.first()[KEY_MUSIC_SECTION_KEY]

    // --- ADDED MISSING FUNCTIONS ---
    suspend fun getServerId(): String? = dataStore.data.first()[KEY_ACTIVE_SERVER_ID]

    suspend fun saveServerId(serverId: String) {
        dataStore.edit { prefs -> prefs[KEY_ACTIVE_SERVER_ID] = serverId }
    }

    suspend fun clearActiveServer() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACTIVE_SERVER_ID)
            prefs.remove(KEY_ACTIVE_SERVER_URL)
            prefs.remove(KEY_MUSIC_SECTION_KEY)
        }
    }
}