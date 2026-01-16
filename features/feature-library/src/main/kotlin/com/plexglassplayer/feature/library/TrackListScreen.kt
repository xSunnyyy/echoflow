package com.plexglassplayer.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.ui.components.*
import com.plexglassplayer.core.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracks") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState is TrackListUiState.Success) {
                        IconButton(onClick = { viewModel.downloadAll() }) {
                            Icon(Icons.Default.Download, "Download All")
                        }
                        IconButton(onClick = { viewModel.playAll() }) {
                            Icon(Icons.Default.PlayArrow, "Play All")
                        }
                    }
                },
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
                is TrackListUiState.Loading -> {
                    LoadingState(message = "Loading tracks...")
                }

                is TrackListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(state.tracks) { index, track ->
                            TrackItem(
                                track = track,
                                trackNumber = index + 1,
                                onClick = { viewModel.playTrack(track) },
                                onDownloadClick = { viewModel.downloadTrack(track) }
                            )
                        }
                    }
                }

                is TrackListUiState.Empty -> {
                    EmptyState(message = "No tracks found in this album")
                }

                is TrackListUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadTracks(isRefresh = true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackItem(
    track: Track,
    trackNumber: Int,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track number
            Text(
                text = "${track.trackNumber ?: trackNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = track.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Duration
            Text(
                text = track.durationMs.formatDuration(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // More options
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Download") },
                        onClick = {
                            onDownloadClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Download, "Download")
                        }
                    )
                }
            }
        }
    }
}
