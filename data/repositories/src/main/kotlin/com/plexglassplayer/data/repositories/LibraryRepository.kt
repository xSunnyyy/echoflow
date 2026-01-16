package com.plexglassplayer.data.repositories

import com.plexglassplayer.core.model.*
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.core.util.suspendRunCatching
import com.plexglassplayer.data.api.dto.toModel
import com.plexglassplayer.data.api.service.PlexApiService
import com.plexglassplayer.data.auth.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val apiService: PlexApiService,
    private val sessionStore: SessionStore,
    private val serverPreferences: ServerPreferences
) {

    suspend fun getArtists(offset: Int = 0, limit: Int = 50): Result<List<Artist>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")

        val url = "$baseUrl/library/sections/$sectionKey/all"
        val response = apiService.getArtists(url, token, offset, limit)
        response.container.metadata.map { it.toModel() }
    }

    suspend fun getAlbums(artistId: String? = null, offset: Int = 0, limit: Int = 50): Result<List<Album>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")

        val url = if (artistId != null) {
            "$baseUrl/library/metadata/$artistId/children"
        } else {
            "$baseUrl/library/sections/$sectionKey/albums"
        }

        val response = apiService.getAlbums(url, token, offset, limit)
        response.container.metadata.map { it.toModel() }
    }

    suspend fun getTracks(albumId: String, offset: Int = 0, limit: Int = 50): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = "$baseUrl/library/metadata/$albumId/children"
        val response = apiService.getTracks(url, token, offset, limit)
        response.container.metadata.map { it.toModel() }
    }

    suspend fun getAllTracks(offset: Int = 0, limit: Int = 100): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")
        val sectionKey = serverPreferences.getMusicSectionKey() ?: throw IllegalStateException("No music section")

        // Get all tracks from the music library section (type=10 means tracks)
        val url = "$baseUrl/library/sections/$sectionKey/all?type=10"
        val response = apiService.getTracks(url, token, offset, limit)
        response.container.metadata.map { it.toModel() }
    }

    suspend fun getPlaylists(): Result<List<Playlist>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = "$baseUrl/playlists"
        val response = apiService.getPlaylists(url, token)
        response.container.metadata.map { it.toModel() }
    }

    suspend fun search(query: String, offset: Int = 0, limit: Int = 50): Result<List<Track>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val baseUrl = serverPreferences.getActiveServerUrl() ?: throw IllegalStateException("No active server")

        val url = "$baseUrl/search"
        val response = apiService.search(url, query, token, offset, limit)
        response.container.metadata.map { it.toModel() }
    }
}
