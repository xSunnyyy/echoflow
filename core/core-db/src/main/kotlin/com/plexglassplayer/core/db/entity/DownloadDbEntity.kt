package com.plexglassplayer.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.plexglassplayer.core.model.DownloadEntity
import com.plexglassplayer.core.model.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadDbEntity(
    @PrimaryKey
    val id: String,
    val serverId: String,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String?,
    val status: String,
    val progressPct: Int,
    val bytesDownloaded: Long,
    val bytesTotal: Long?,
    val filePath: String?,
    val errorMessage: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

fun DownloadDbEntity.toModel(): DownloadEntity {
    return DownloadEntity(
        id = id,
        serverId = serverId,
        trackId = trackId,
        title = title,
        artist = artist,
        album = album,
        artworkUrl = artworkUrl,
        status = DownloadStatus.valueOf(status),
        progressPct = progressPct,
        bytesDownloaded = bytesDownloaded,
        bytesTotal = bytesTotal,
        filePath = filePath,
        errorMessage = errorMessage
    )
}

fun DownloadEntity.toDbEntity(createdAt: Long, updatedAt: Long): DownloadDbEntity {
    return DownloadDbEntity(
        id = id,
        serverId = serverId,
        trackId = trackId,
        title = title,
        artist = artist,
        album = album,
        artworkUrl = artworkUrl,
        status = status.name,
        progressPct = progressPct,
        bytesDownloaded = bytesDownloaded,
        bytesTotal = bytesTotal,
        filePath = filePath,
        errorMessage = errorMessage,
        createdAtEpochMs = createdAt,
        updatedAtEpochMs = updatedAt
    )
}
