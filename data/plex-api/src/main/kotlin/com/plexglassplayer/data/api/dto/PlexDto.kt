package com.plexglassplayer.data.api.dto

import com.plexglassplayer.core.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- NEW: Identity DTOs ---
@Serializable
data class IdentityResponse(
    @SerialName("MediaContainer") val container: IdentityContainer
)

@Serializable
data class IdentityContainer(
    @SerialName("machineIdentifier") val machineIdentifier: String? = null
)

// Auth
@Serializable
data class PinResponse(
    val id: String,
    val code: String,
    @SerialName("authToken") val authToken: String? = null
)

// Resources
@Serializable
data class DeviceDto(
    @SerialName("clientIdentifier") val clientIdentifier: String,
    @SerialName("name") val name: String,
    @SerialName("owned") val owned: Boolean = false,
    @SerialName("connections") val connections: List<ConnectionDto> = emptyList()
)

@Serializable
data class ConnectionDto(
    @SerialName("uri") val uri: String,
    @SerialName("local") val local: Boolean = false,
    @SerialName("protocol") val protocol: String = "http",
    @SerialName("relay") val relay: Boolean = false
)

fun DeviceDto.toModel(): PlexServer {
    return PlexServer(
        id = clientIdentifier,
        name = name,
        owned = owned,
        connections = connections.map { it.toModel() },
        preferredConnectionUrl = connections.firstOrNull()?.uri
    )
}

fun ConnectionDto.toModel(): ServerConnection {
    return ServerConnection(uri = uri, isLocal = local, isSecure = protocol == "https", isRelay = relay)
}

// Library
@Serializable
data class LibrarySectionsResponse(
    @SerialName("MediaContainer") val container: LibrarySectionsContainer
)

@Serializable
data class LibrarySectionsContainer(
    @SerialName("Directory") val directories: List<LibrarySectionDto> = emptyList()
)

@Serializable
data class LibrarySectionDto(
    val key: String,
    val title: String,
    val type: String
)

// Artist
@Serializable
data class ArtistsResponse(
    @SerialName("MediaContainer") val container: ArtistsContainer
)

@Serializable
data class ArtistsContainer(
    @SerialName("Metadata") val metadata: List<ArtistDto> = emptyList(),
    val size: Int = 0
)

@Serializable
data class ArtistDto(
    @SerialName("ratingKey") val ratingKey: String,
    val title: String,
    val thumb: String? = null,
    val art: String? = null
)

fun ArtistDto.toModel(): Artist {
    return Artist(id = ratingKey, name = title, artUrl = art, thumbUrl = thumb)
}

// Album
@Serializable
data class AlbumsResponse(
    @SerialName("MediaContainer") val container: AlbumsContainer
)

@Serializable
data class AlbumsContainer(
    @SerialName("Metadata") val metadata: List<AlbumDto> = emptyList(),
    val size: Int = 0
)

@Serializable
data class AlbumDto(
    @SerialName("ratingKey") val ratingKey: String,
    val title: String,
    @SerialName("parentTitle") val parentTitle: String? = null,
    val year: Int? = null,
    val thumb: String? = null
)

fun AlbumDto.toModel(): Album {
    return Album(id = ratingKey, title = title, artistName = parentTitle ?: "Unknown Artist", year = year, artUrl = thumb)
}

// Track
@Serializable
data class TracksResponse(
    @SerialName("MediaContainer") val container: TracksContainer
)

@Serializable
data class TracksContainer(
    @SerialName("Metadata") val metadata: List<TrackDto> = emptyList(),
    val size: Int = 0
)

@Serializable
data class TrackDto(
    @SerialName("ratingKey") val ratingKey: String,
    val key: String,
    val title: String,
    @SerialName("grandparentTitle") val grandparentTitle: String? = null,
    @SerialName("parentTitle") val parentTitle: String? = null,
    val duration: Long? = null,
    val index: Int? = null,
    val thumb: String? = null,
    @SerialName("Media") val media: List<MediaDto>? = null,

    // --- CRITICAL FOR DELETION ---
    @SerialName("playlistItemID") val playlistItemId: String? = null,
    @SerialName("id") val id: Int? = null
)

@Serializable
data class MediaDto(
    @SerialName("Part") val parts: List<PartDto>? = null
)

@Serializable
data class PartDto(
    val key: String? = null,
    val file: String? = null
)

fun TrackDto.toModel(): Track {
    val mediaKey = media?.firstOrNull()?.parts?.firstOrNull()?.key
    return Track(
        id = ratingKey,
        title = title,
        artistName = grandparentTitle ?: "Unknown Artist",
        albumTitle = parentTitle ?: "Unknown Album",
        durationMs = duration ?: 0L,
        trackNumber = index,
        artUrl = thumb,
        streamKey = mediaKey ?: key
    )
}

// Playlist
@Serializable
data class PlaylistsResponse(
    @SerialName("MediaContainer") val container: PlaylistsContainer
)

@Serializable
data class PlaylistsContainer(
    @SerialName("Metadata") val metadata: List<PlaylistDto> = emptyList()
)

@Serializable
data class PlaylistDto(
    @SerialName("ratingKey") val ratingKey: String,
    val title: String,
    @SerialName("leafCount") val leafCount: Int = 0,
    val thumb: String? = null
)

fun PlaylistDto.toModel(): Playlist {
    return Playlist(id = ratingKey, title = title, trackCount = leafCount, artUrl = thumb)
}
