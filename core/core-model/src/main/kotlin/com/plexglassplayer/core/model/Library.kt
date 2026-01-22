package com.plexglassplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val id: String,
    val name: String,
    val artUrl: String?,
    val thumbUrl: String?
)

@Serializable
data class Album(
    val id: String,
    val title: String,
    val artistName: String,
    val year: Int?,
    val artUrl: String?
)

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artistName: String,
    val albumTitle: String,
    val durationMs: Long,
    val trackNumber: Int?,
    val artUrl: String?,
    val streamKey: String, // Plex key/path needed to build play URL
    val playlistItemId: String? = null // Playlist item ID for deletion (only set when track is from a playlist)
)

@Serializable
data class Playlist(
    val id: String,
    val title: String,
    val trackCount: Int,
    val artUrl: String?
)

@Serializable
data class Genre(
    val id: String,
    val title: String
)
