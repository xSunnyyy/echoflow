package com.plexglassplayer.data.repositories

import com.plexglassplayer.core.model.*
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.core.util.suspendRunCatching
import com.plexglassplayer.data.api.dto.toModel
import com.plexglassplayer.data.api.service.PlexApiService
import com.plexglassplayer.data.auth.SessionStore
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val apiService: PlexApiService,
    private val sessionStore: SessionStore,
    private val serverPreferences: ServerPreferences
) {

    private fun getAbsoluteUrl(path: String?, baseUrl: String, token: String): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http")) return path
        val cleanBase = baseUrl.trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$cleanBase$cleanPath?X-Plex-Token=$token"
    }

    suspend fun getRecentTracks(limit: Int = 10): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")
        val url = "$baseUrl/library/sections/$sectionKey/all?type=10&sort=lastViewedAt:desc"
        val response = apiService.getTracks(url, token, 0, limit)
        response.container.metadata.map { dto ->
            val track = dto.toModel()
            track.copy(artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token))
        }
    }

    suspend fun getArtists(offset: Int = 0, limit: Int = 50): Result<List<Artist>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")
        val url = "$baseUrl/library/sections/$sectionKey/all"
        val response = apiService.getArtists(url, token, offset, limit)
        response.container.metadata.map { dto ->
            val artist = dto.toModel()
            artist.copy(
                thumbUrl = getAbsoluteUrl(artist.thumbUrl, baseUrl, token),
                artUrl = getAbsoluteUrl(artist.artUrl, baseUrl, token)
            )
        }
    }

    suspend fun getAlbums(artistId: String? = null, offset: Int = 0, limit: Int = 50): Result<List<Album>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")
        val url = if (artistId != null) "$baseUrl/library/metadata/$artistId/children" else "$baseUrl/library/sections/$sectionKey/albums"
        val response = apiService.getAlbums(url, token, offset, limit)
        response.container.metadata.map { dto ->
            val album = dto.toModel()
            album.copy(artUrl = getAbsoluteUrl(album.artUrl, baseUrl, token))
        }
    }

    suspend fun getTracks(albumId: String, offset: Int = 0, limit: Int = 50): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val url = "$baseUrl/library/metadata/$albumId/children"
        val response = apiService.getTracks(url, token, offset, limit)
        response.container.metadata.map { dto ->
            val track = dto.toModel()
            track.copy(artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token))
        }
    }

    suspend fun getAllTracks(offset: Int = 0, limit: Int = 100): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")
        val url = "$baseUrl/library/sections/$sectionKey/all?type=10"
        val response = apiService.getTracks(url, token, offset, limit)
        response.container.metadata.map { dto ->
            val track = dto.toModel()
            track.copy(artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token))
        }
    }

    suspend fun getPlaylists(): Result<List<Playlist>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val url = "$baseUrl/playlists"
        val response = apiService.getPlaylists(url, token)
        response.container.metadata.map { dto ->
            val playlist = dto.toModel()
            playlist.copy(artUrl = getAbsoluteUrl(playlist.artUrl, baseUrl, token))
        }
    }

    suspend fun getPlaylistItems(playlistId: String, offset: Int = 0, limit: Int = 500): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val url = "$baseUrl/playlists/$playlistId/items"
        val response = apiService.getTracks(url, token, offset, limit)
        response.container.metadata.map { dto ->
            val track = dto.toModel()
            track.copy(artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token))
        }
    }

    suspend fun search(query: String, offset: Int = 0, limit: Int = 50): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val url = "$baseUrl/search"
        val response = apiService.search(url, query, token, offset, limit)
        response.container.metadata.map { dto ->
            val track = dto.toModel()
            track.copy(artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token))
        }
    }

    private suspend fun getTrackUri(trackId: String): String {
        val machineId = serverPreferences.getServerId()
        return if (!machineId.isNullOrEmpty()) {
            "server://$machineId/com.plexapp.plugins.library/library/metadata/$trackId"
        } else {
            "library:///library/metadata/$trackId"
        }
    }

    suspend fun addToPlaylist(playlistId: String, track: Track): Result<Unit> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val uri = getTrackUri(track.id)
        val url = "$baseUrl/playlists/$playlistId/items"
        Timber.d("Adding to playlist: URL=$url, URI=$uri")
        apiService.addToPlaylist(url, uri, token)
    }

    suspend fun createPlaylist(title: String, firstTrack: Track): Result<Unit> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val uri = getTrackUri(firstTrack.id)
        val url = "$baseUrl/playlists"
        Timber.d("Creating playlist '$title' with URI=$uri")
        apiService.createPlaylist(url = url, title = title, uri = uri, token = token)
    }

    // --- FIXED: No more checks, just call API ---
    suspend fun deletePlaylist(playlistId: String): Result<Unit> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val url = "$baseUrl/playlists/$playlistId"

        // Retrofit throws exception if non-2xx
        apiService.deletePlaylist(url, token)
    }
}