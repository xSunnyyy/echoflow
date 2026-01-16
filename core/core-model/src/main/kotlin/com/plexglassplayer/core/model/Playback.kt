package com.plexglassplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class QueueState(
    val queueId: String,
    val items: List<QueueItem>,
    val currentIndex: Int,
    val shuffle: Boolean,
    val repeatMode: RepeatMode
)

@Serializable
data class QueueItem(
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String?,
    val source: MediaSourceType,
    val uri: String
)

@Serializable
enum class MediaSourceType {
    STREAM,
    LOCAL
}

@Serializable
enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

sealed class PlayerState {
    data object Idle : PlayerState()
    data class Preparing(val source: String) : PlayerState()
    data object Playing : PlayerState()
    data object Paused : PlayerState()
    data object Buffering : PlayerState()
    data object Ended : PlayerState()
    data class Error(val type: String) : PlayerState()
}

data class PlaybackUiState(
    val state: PlayerState,
    val positionMs: Long,
    val bufferedMs: Long,
    val durationMs: Long,
    val currentItem: QueueItem?
)
