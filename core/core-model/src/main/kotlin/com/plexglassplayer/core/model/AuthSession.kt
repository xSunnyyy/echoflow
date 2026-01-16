package com.plexglassplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(
    val userId: String,
    val accessToken: String,
    val tokenType: TokenType,
    val expiresAtEpochMs: Long?, // nullable if legacy/unknown
    val lastRefreshEpochMs: Long?
)

@Serializable
enum class TokenType {
    LEGACY,
    JWT
}
