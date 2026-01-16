package com.plexglassplayer.feature.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// Ensure these point to the actual models in your core module
import com.plexglassplayer.core.model.Download 
import com.plexglassplayer.core.model.DownloadStatus
import com.plexglassplayer.core.ui.components.AlbumArt
import com.plexglassplayer.core.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when (val state = uiState) {
            is DownloadsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DownloadsUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No downloads",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is DownloadsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Active downloads
                    if (state.activeDownloads.isNotEmpty()) {
                        item {
                            Text(
                                "Downloading",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(state.activeDownloads) { download ->
                            DownloadItem(
                                download = download,
                                onCancelClick = { viewModel.cancelDownload(download.id) },
                                onRetryClick = null,
                                onDeleteClick = null
                            )
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // Failed downloads
                    if (state.failedDownloads.isNotEmpty()) {
                        item {
                            Text(
                                "Failed",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(state.failedDownloads) { download ->
                            DownloadItem(
                                download = download,
                                onCancelClick = null,
                                onRetryClick = { viewModel.retryDownload(download.id) },
                                onDeleteClick = { viewModel.deleteDownload(download.id) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // Completed downloads
                    if (state.completedDownloads.isNotEmpty()) {
                        item {
                            Text(
                                "Completed",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(state.completedDownloads) { download ->
                            DownloadItem(
                                download = download,
                                onCancelClick = null,
                                onRetryClick = null,
                                onDeleteClick = { viewModel.deleteDownload(download.id) }
                            )
                        }
                    }
                }
            }
            // Fix: Capture errorMessage to local variable to allow smart casting
            is DownloadsUiState.Error -> {
                val errorMsg = state.errorMessage
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMsg ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadItem(
    download: Download, // Changed from DownloadEntity to match core model
    onCancelClick: (() -> Unit)?,
    onRetryClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            AlbumArt(
                artUrl = download.artworkUrl,
                size = 56.dp,
                cornerRadius = 4.dp
            )

            // Track info and progress
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${download.artist} • ${download.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Progress indicator for active downloads
                if (download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.QUEUED) {
                    LinearProgressIndicator(
                        progress = { download.progressPct / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${download.progressPct}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Error message for failed downloads - Fix: Use local variable for smart cast
                val itemError = download.errorMessage
                if (download.status == DownloadStatus.FAILED && itemError != null) {
                    Text(
                        text = itemError,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                onCancelClick?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                }

                onRetryClick?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Refresh, "Retry")
                    }
                }

                onDeleteClick?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }
    }
}
