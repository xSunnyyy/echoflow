package com.plexglassplayer.data.repositories

import com.plexglassplayer.core.model.PlexServer
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.core.util.suspendRunCatching
import com.plexglassplayer.data.api.dto.toModel
import com.plexglassplayer.data.api.service.PlexApiService
import com.plexglassplayer.data.auth.SessionStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
    private val apiService: PlexApiService,
    private val sessionStore: SessionStore
) {

    suspend fun getServers(): Result<List<PlexServer>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val clientId = sessionStore.clientId

        val response = apiService.getResources(token, clientId)
        response.container.devices
            .filter { it.name.isNotEmpty() && it.connections.isNotEmpty() }
            .map { it.toModel() }
    }

    suspend fun getLibrarySections(serverUrl: String): Result<List<String>> = suspendRunCatching {
        val token = sessionStore.getAccessToken() ?: throw IllegalStateException("No auth token")
        val url = "$serverUrl/library/sections"

        val response = apiService.getLibrarySections(url, token)
        response.container.directories
            .filter { it.type == "artist" }
            .map { it.key }
    }
}
