package com.plexglassplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class RecentPlay(
    val serverId: String,
    val trackId: String,
    val playedAtEpochMs: Long,
    val lastPositionMs: Long
)
