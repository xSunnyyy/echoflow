package com.plexglassplayer.data.repositories

import com.plexglassplayer.core.model.*
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.core.util.suspendRunCatching
import com.plexglassplayer.data.api.dto.*
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

    // Helper to safely combine a base URL and a path
    private fun sanitizeUrl(baseUrl: String, path: String): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$cleanBase$cleanPath"
    }

    private fun getAbsoluteUrl(path: String?, baseUrl: String, token: String): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http")) return path
        val url = sanitizeUrl(baseUrl, path)
        return "$url?X-Plex-Token=$token"
    }

    suspend fun getRecentTracks(limit: Int = 10): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")

        val url = sanitizeUrl(baseUrl, "library/sections/$sectionKey/all?type=10&sort=lastViewedAt:desc")
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

        val url = sanitizeUrl(baseUrl, "library/sections/$sectionKey/all")
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

        val path = if (artistId != null) "library/metadata/$artistId/children" else "library/sections/$sectionKey/albums"
        val url = sanitizeUrl(baseUrl, path)

        val response = apiService.getAlbums(url, token, offset, limit)
        response.container.metadata.map { dto ->
            val album = dto.toModel()
            album.copy(artUrl = getAbsoluteUrl(album.artUrl, baseUrl, token))
        }
    }

    suspend fun getTracks(albumId: String, offset: Int = 0, limit: Int = 50): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = sanitizeUrl(baseUrl, "library/metadata/$albumId/children")
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

        val url = sanitizeUrl(baseUrl, "library/sections/$sectionKey/all?type=10")
        val response = apiService.getTracks(url, token, offset, limit)
        response.container.metadata.map { dto ->
            val track = dto.toModel()
            track.copy(artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token))
        }
    }

    suspend fun getPlaylists(): Result<List<Playlist>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = sanitizeUrl(baseUrl, "playlists")
        val response = apiService.getPlaylists(url, token)
        response.container.metadata.map { dto ->
            val playlist = dto.toModel()
            playlist.copy(artUrl = getAbsoluteUrl(playlist.artUrl, baseUrl, token))
        }
    }

    suspend fun getPlaylistItems(playlistId: String, offset: Int = 0, limit: Int = 500): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = sanitizeUrl(baseUrl, "playlists/$playlistId/items")
        val response = apiService.getTracks(url, token, offset, limit)

        response.container.metadata.map { dto ->
            val track = dto.toModel()
            // Map the playlist Item ID (dto.id) so deletion works
            track.copy(
                playlistItemId = dto.playlistItemId ?: dto.id?.toString(),
                artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token)
            )
        }
    }

    suspend fun search(query: String, offset: Int = 0, limit: Int = 50): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = sanitizeUrl(baseUrl, "search")
        val response = apiService.search(url, query, token, offset, limit)
        response.container.metadata.map { dto ->
            val track = dto.toModel()
            track.copy(artUrl = getAbsoluteUrl(track.artUrl, baseUrl, token))
        }
    }

    // --- HELPER to get URI and fix missing Server ID ---
    private suspend fun getTrackUri(trackId: String, token: String, baseUrl: String): String {
        var machineId = serverPreferences.getServerId()

        if (machineId.isNullOrEmpty()) {
            try {
                // Fetch identity if missing
                val identityUrl = sanitizeUrl(baseUrl, "identity")
                val identity = apiService.getIdentity(identityUrl, token)
                machineId = identity.container.machineIdentifier
                if (machineId != null) {
                    serverPreferences.saveServerId(machineId)
                }
            } catch (e: Exception) {
                Timber.e("Failed to fetch machine ID: $e")
            }
        }

        return if (!machineId.isNullOrEmpty()) {
            "server://$machineId/com.plexapp.plugins.library/library/metadata/$trackId"
        } else {
            "library:///library/metadata/$trackId"
        }
    }

    suspend fun addToPlaylist(playlistId: String, track: Track): Result<Unit> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val uri = getTrackUri(track.id, token, baseUrl)
        val url = sanitizeUrl(baseUrl, "playlists/$playlistId/items")

        Timber.d("Adding to playlist: URL=$url, URI=$uri")
        apiService.addToPlaylist(url, uri, token)
    }

    suspend fun createPlaylist(title: String, firstTrack: Track): Result<Unit> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val uri = getTrackUri(firstTrack.id, token, baseUrl)
        val url = sanitizeUrl(baseUrl, "playlists")

        Timber.d("Creating playlist '$title' with URI=$uri")
        apiService.createPlaylist(url = url, title = title, uri = uri, token = token)
    }

    suspend fun deletePlaylist(playlistId: String): Result<Unit> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = sanitizeUrl(baseUrl, "playlists/$playlistId")
        apiService.deletePlaylist(url, token)
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, playlistItemId: String): Result<Unit> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        // FIX: Ensure no double slashes
        val url = sanitizeUrl(baseUrl, "playlists/$playlistId/items/$playlistItemId")

        Timber.d("Removing track from playlist: $url")
        apiService.removeFromPlaylist(url, token)
    }
}