package com.plexglassplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    FAILED,
    COMPLETED
}

@Serializable
data class DownloadEntity(
    val id: String, // trackId or composite (serverId+trackId)
    val serverId: String,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String?,
    val status: DownloadStatus,
    val progressPct: Int,
    val bytesDownloaded: Long,
    val bytesTotal: Long?,
    val filePath: String?,
    val errorMessage: String?
)
