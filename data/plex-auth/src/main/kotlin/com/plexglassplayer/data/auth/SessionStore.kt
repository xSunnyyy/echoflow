package com.plexglassplayer.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.plexglassplayer.core.model.AuthSession
import com.plexglassplayer.core.model.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_TOKEN_TYPE = stringPreferencesKey("token_type")
        private val KEY_EXPIRES_AT = longPreferencesKey("expires_at")
        private val KEY_LAST_REFRESH = longPreferencesKey("last_refresh")
        private val KEY_CLIENT_ID = stringPreferencesKey("client_id")
    }

    val clientId: String by lazy {
        // Generate a stable client ID for this installation
        UUID.randomUUID().toString()
    }

    val sessionFlow: Flow<AuthSession?> = dataStore.data.map { prefs ->
        val userId = prefs[KEY_USER_ID]
        val accessToken = prefs[KEY_ACCESS_TOKEN]
        val tokenTypeStr = prefs[KEY_TOKEN_TYPE]

        if (userId != null && accessToken != null && tokenTypeStr != null) {
            AuthSession(
                userId = userId,
                accessToken = accessToken,
                tokenType = TokenType.valueOf(tokenTypeStr),
                expiresAtEpochMs = prefs[KEY_EXPIRES_AT],
                lastRefreshEpochMs = prefs[KEY_LAST_REFRESH]
            )
        } else {
            null
        }
    }

    suspend fun saveSession(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = session.userId
            prefs[KEY_ACCESS_TOKEN] = session.accessToken
            prefs[KEY_TOKEN_TYPE] = session.tokenType.name
            session.expiresAtEpochMs?.let { prefs[KEY_EXPIRES_AT] = it }
            session.lastRefreshEpochMs?.let { prefs[KEY_LAST_REFRESH] = it }
        }
    }

    suspend fun getAccessToken(): String? {
        var token: String? = null
        dataStore.data.collect { prefs ->
            token = prefs[KEY_ACCESS_TOKEN]
        }
        return token
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_TOKEN_TYPE)
            prefs.remove(KEY_EXPIRES_AT)
            prefs.remove(KEY_LAST_REFRESH)
        }
    }
}
