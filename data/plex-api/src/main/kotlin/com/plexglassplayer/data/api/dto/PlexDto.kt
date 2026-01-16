package com.plexglassplayer.data.api.dto

import com.plexglassplayer.core.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Plex PIN auth DTOs
@Serializable
data class PinResponse(
    val id: String,
    val code: String,
    @SerialName("authToken") val authToken: String? = null
)

// Plex resources (servers) DTOs
@Serializable
data class ResourcesResponse(
    @SerialName("MediaContainer") val container: ResourcesContainer
)

@Serializable
data class ResourcesContainer(
    @SerialName("Device") val devices: List<DeviceDto>
)

@Serializable
data class DeviceDto(
    @SerialName("clientIdentifier") val clientIdentifier: String,
    val name: String,
    val owned: Boolean = false,
    @SerialName("Connection") val connections: List<ConnectionDto> = emptyList()
)

@Serializable
data class ConnectionDto(
    val uri: String,
    val local: Boolean = false,
    @SerialName("protocol") val protocol: String = "http",
    val relay: Boolean = false
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
    return ServerConnection(
        uri = uri,
        isLocal = local,
        isSecure = protocol == "https",
        isRelay = relay
    )
}

// Library DTOs
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
    val type: String // "artist", "movie", "show", etc.
)

// Artist DTOs
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
    return Artist(
        id = ratingKey,
        name = title,
        artUrl = art,
        thumbUrl = thumb
    )
}

// Album DTOs
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
    return Album(
        id = ratingKey,
        title = title,
        artistName = parentTitle ?: "Unknown Artist",
        year = year,
        artUrl = thumb
    )
}

// Track DTOs
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
    @SerialName("grandparentTitle") val grandparentTitle: String? = null, // Artist
    @SerialName("parentTitle") val parentTitle: String? = null, // Album
    val duration: Long? = null,
    val index: Int? = null,
    val thumb: String? = null
)

fun TrackDto.toModel(): Track {
    return Track(
        id = ratingKey,
        title = title,
        artistName = grandparentTitle ?: "Unknown Artist",
        albumTitle = parentTitle ?: "Unknown Album",
        durationMs = duration ?: 0L,
        trackNumber = index,
        artUrl = thumb,
        streamKey = key
    )
}

// Playlist DTOs
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
    return Playlist(
        id = ratingKey,
        title = title,
        trackCount = leafCount,
        artUrl = thumb
    )
}
