package com.plexglassplayer.data.auth

import android.net.Uri
import com.plexglassplayer.core.model.AuthSession
import com.plexglassplayer.core.model.TokenType
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.core.util.suspendRunCatching
import com.plexglassplayer.data.api.service.PlexApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: PlexApiService,
    private val sessionStore: SessionStore
) {

    val sessionFlow: Flow<AuthSession?> = sessionStore.sessionFlow

    /**
     * Start PIN-based OAuth flow
     * Returns the PIN code and auth URL to open in browser
     */
    suspend fun startPinAuth(): Result<PinAuthData> = suspendRunCatching {
        val clientId = sessionStore.clientId
        Timber.d("Starting PIN auth with clientId: $clientId")

        val pinResponse = apiService.createPin(
            clientId = clientId,
            product = "PlexGlassPlayer",
            version = "1.0"
        )

        val authUrl = buildAuthUrl(pinResponse.code, clientId)

        PinAuthData(
            pinId = pinResponse.id,
            pinCode = pinResponse.code,
            authUrl = authUrl
        )
    }

    /**
     * Poll the PIN endpoint until user completes auth
     * Call this after opening the browser
     */
    suspend fun pollForAuth(pinId: String, maxAttempts: Int = 60): Result<AuthSession> {
        val clientId = sessionStore.clientId
        var attempts = 0

        while (attempts < maxAttempts) {
            delay(2000) // Poll every 2 seconds

            val result = suspendRunCatching {
                apiService.checkPin(pinId, clientId)
            }

            when (result) {
                is Result.Success -> {
                    val authToken = result.data.authToken
                    if (!authToken.isNullOrEmpty()) {
                        Timber.d("Auth successful, received token")

                        val session = AuthSession(
                            userId = pinId, // Will be replaced with actual user ID later
                            accessToken = authToken,
                            tokenType = TokenType.LEGACY,
                            expiresAtEpochMs = null,
                            lastRefreshEpochMs = System.currentTimeMillis()
                        )

                        sessionStore.saveSession(session)
                        return Result.Success(session)
                    }
                }
                is Result.Error -> {
                    Timber.w("Error checking PIN: ${result.exception.message}")
                }
                else -> {}
            }

            attempts++
        }

        return Result.Error(Exception("Auth timeout: User did not complete authentication"))
    }

    /**
     * Handle OAuth callback from deep link
     */
    suspend fun handleOAuthCallback(uri: Uri): Result<AuthSession> = suspendRunCatching {
        Timber.d("Handling OAuth callback: $uri")

        // Extract token from URI if using direct callback
        val token = uri.getQueryParameter("token")
            ?: uri.getQueryParameter("authToken")
            ?: throw IllegalArgumentException("No token in callback URI")

        val session = AuthSession(
            userId = "user", // Placeholder, fetch actual user ID later
            accessToken = token,
            tokenType = TokenType.LEGACY,
            expiresAtEpochMs = null,
            lastRefreshEpochMs = System.currentTimeMillis()
        )

        sessionStore.saveSession(session)
        session
    }

    suspend fun signOut() {
        sessionStore.clearSession()
    }

    private fun buildAuthUrl(pinCode: String, clientId: String): String {
        return Uri.parse("https://app.plex.tv/auth#")
            .buildUpon()
            .appendQueryParameter("clientID", clientId)
            .appendQueryParameter("code", pinCode)
            .appendQueryParameter("context[device][product]", "PlexGlassPlayer")
            .build()
            .toString()
    }
}

data class PinAuthData(
    val pinId: String,
    val pinCode: String,
    val authUrl: String
)
