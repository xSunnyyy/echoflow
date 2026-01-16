package com.plexglassplayer.feature.server

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plexglassplayer.core.model.PlexServer
import com.plexglassplayer.core.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionScreen(
    onServerSelected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServerSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is ServerSelectionUiState.ServerSelected) {
            onServerSelected()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a Server") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ServerSelectionUiState.Loading -> {
                    LoadingState(message = "Loading servers...")
                }

                is ServerSelectionUiState.Success -> {
                    ServerList(
                        servers = state.servers,
                        onServerClick = { viewModel.selectServer(it) }
                    )
                }

                is ServerSelectionUiState.Selecting -> {
                    ServerList(
                        servers = listOf(state.server),
                        onServerClick = {},
                        selectingServerId = state.server.id
                    )
                }

                is ServerSelectionUiState.Empty -> {
                    EmptyState(message = "No Plex servers found.\n\nMake sure you have access to at least one Plex server.")
                }

                is ServerSelectionUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadServers() }
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ServerList(
    servers: List<PlexServer>,
    onServerClick: (PlexServer) -> Unit,
    selectingServerId: String? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(servers) { server ->
            ServerItem(
                server = server,
                onClick = { onServerClick(server) },
                isSelecting = server.id == selectingServerId
            )
        }
    }
}

@Composable
private fun ServerItem(
    server: PlexServer,
    onClick: () -> Unit,
    isSelecting: Boolean,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isSelecting, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = buildServerStatusText(server),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun buildServerStatusText(server: PlexServer): String {
    val owned = if (server.owned) "Owned" else "Shared"
    val connectionsCount = server.connections.size
    val localConnection = server.connections.any { it.isLocal }

    return buildString {
        append(owned)
        if (localConnection) {
            append(" • Local")
        }
        append(" • $connectionsCount connection${if (connectionsCount != 1) "s" else ""}")
    }
}
