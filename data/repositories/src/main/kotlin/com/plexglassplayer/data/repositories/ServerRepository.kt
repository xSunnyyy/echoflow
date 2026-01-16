package com.plexglassplayer.data.repositories

import com.plexglassplayer.core.model.PlexServer
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.core.util.suspendRunCatching
import com.plexglassplayer.data.api.dto.toModel
import com.plexglassplayer.data.api.service.PlexApiService
import com.plexglassplayer.data.auth.SessionStore
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
    private val apiService: PlexApiService,
    private val sessionStore: SessionStore
) {

    suspend fun getServers(): Result<List<PlexServer>> = suspendRunCatching {
        val token = sessionStore.getAccessToken()
            ?: throw IllegalStateException("No auth token")
        val clientId = sessionStore.clientId

        // ✅ api/v2/resources returns a JSON array -> List<DeviceDto>
        val devices = apiService.getResources(
            token = token,
            clientId = clientId
        )

        Timber.d(
            "Plex resources: devices=${devices.size} connectionsTotal=${devices.sumOf { it.connections.size }}"
        )

        devices
            .filter { it.name.isNotBlank() }
            // keep servers with at least 1 connection url
            .filter { it.connections.isNotEmpty() }
            .map { it.toModel() }
    }

    suspend fun getLibrarySections(serverUrl: String): Result<List<String>> = suspendRunCatching {
        val token = sessionStore.getAccessToken()
            ?: throw IllegalStateException("No auth token")

        // Important: serverUrl should already include scheme + host (e.g. https://192.168.1.10:32400)
        val url = "$serverUrl/library/sections"

        val response = apiService.getLibrarySections(url, token)

        response.container.directories
            .filter { it.type == "artist" }
            .map { it.key }
    }
}
