package com.plexglassplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PlexServer(
    val id: String, // machineIdentifier
    val name: String,
    val owned: Boolean,
    val connections: List<ServerConnection>,
    val preferredConnectionUrl: String?
)

@Serializable
data class ServerConnection(
    val uri: String,
    val isLocal: Boolean,
    val isSecure: Boolean,
    val isRelay: Boolean
)
