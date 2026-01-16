package com.plexglassplayer.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.plexglassplayer.core.model.RecentPlay

@Entity(tableName = "recent_plays")
data class RecentPlayDbEntity(
    @PrimaryKey(autoGenerate = true)
    val rowId: Long = 0,
    val serverId: String,
    val trackId: String,
    val playedAtEpochMs: Long,
    val lastPositionMs: Long
)

fun RecentPlayDbEntity.toModel(): RecentPlay {
    return RecentPlay(
        serverId = serverId,
        trackId = trackId,
        playedAtEpochMs = playedAtEpochMs,
        lastPositionMs = lastPositionMs
    )
}

fun RecentPlay.toDbEntity(): RecentPlayDbEntity {
    return RecentPlayDbEntity(
        serverId = serverId,
        trackId = trackId,
        playedAtEpochMs = playedAtEpochMs,
        lastPositionMs = lastPositionMs
    )
}
