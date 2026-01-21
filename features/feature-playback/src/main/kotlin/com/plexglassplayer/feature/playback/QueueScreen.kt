package com.plexglassplayer.feature.playback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plexglassplayer.core.model.QueueItem
import com.plexglassplayer.core.model.RepeatMode
import com.plexglassplayer.core.ui.components.AlbumArt
import com.plexglassplayer.core.ui.components.GlassCard
import com.plexglassplayer.core.util.formatDuration
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    playbackManager: PlaybackManager,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNowPlayingClick: () -> Unit = {}
) {
    val queue by playbackManager.queue.collectAsState()
    val currentIndex by playbackManager.currentIndex.collectAsState()
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val shuffleEnabled by playbackManager.shuffleEnabled.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()
    val duration by playbackManager.duration.collectAsState()

    var currentPosition by remember { mutableLongStateOf(0L) }

    // Update position while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Queue") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (queue.isNotEmpty()) {
                        IconButton(onClick = { playbackManager.clearQueue() }) {
                            Icon(Icons.Default.Clear, "Clear Queue")
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (queue.isEmpty()) {
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
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Queue is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Now Playing Card at top
                if (currentTrack != null) {
                    item {
                        NowPlayingCard(
                            track = currentTrack!!,
                            isPlaying = isPlaying,
                            duration = duration,
                            currentPosition = currentPosition,
                            shuffleEnabled = shuffleEnabled,
                            repeatMode = repeatMode,
                            onPlayPause = { playbackManager.playPause() },
                            onNext = { playbackManager.playNext() },
                            onPrevious = { playbackManager.playPrevious() },
                            onSeek = { playbackManager.seekTo(it) },
                            onToggleShuffle = { playbackManager.toggleShuffle() },
                            onToggleRepeat = { playbackManager.toggleRepeatMode() },
                            onClick = onNowPlayingClick
                        )
                    }
                }

                // Upcoming Songs Card
                val upcomingTracks = queue.drop(currentIndex + 1)
                if (upcomingTracks.isNotEmpty()) {
                    item {
                        UpcomingSongsCard(
                            tracks = upcomingTracks,
                            startIndex = currentIndex + 1,
                            onTrackClick = { index -> playbackManager.playFromQueue(index) },
                            onRemoveClick = { index -> playbackManager.removeFromQueue(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowPlayingCard(
    track: QueueItem,
    isPlaying: Boolean,
    duration: Long,
    currentPosition: Long,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "Now Playing" title above the card
        Text(
            text = "Now Playing",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            blurRadius = 40.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large album artwork on the left
                AlbumArt(
                    artUrl = track.artworkUrl,
                    size = 160.dp,
                    cornerRadius = 12.dp
                )

                // All info and controls on the right
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Track info at top
                    Column {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = track.album,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Progress bar in middle
                    Slider(
                        value = if (duration > 0) (currentPosition.toFloat() / duration) else 0f,
                        onValueChange = { value ->
                            onSeek((value * duration).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    // Controls at bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPrevious,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                "Previous",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleShuffle,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                "Shuffle",
                                tint = if (shuffleEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        FilledIconButton(
                            onClick = onPlayPause,
                            modifier = Modifier.size(44.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleRepeat,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    RepeatMode.ONE -> Icons.Default.RepeatOne
                                    else -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = when (repeatMode) {
                                    RepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNext,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                "Next",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingSongsCard(
    tracks: List<QueueItem>,
    startIndex: Int,
    onTrackClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // "Upcoming Songs" label
            Text(
                text = "Upcoming Songs",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // List of upcoming tracks
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tracks.forEachIndexed { relativeIndex, track ->
                    val absoluteIndex = startIndex + relativeIndex
                    UpcomingTrackItem(
                        track = track,
                        position = relativeIndex + 1,
                        onClick = { onTrackClick(absoluteIndex) },
                        onRemoveClick = { onRemoveClick(absoluteIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingTrackItem(
    track: QueueItem,
    position: Int,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position number
        Text(
            text = position.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )

        // Album art
        AlbumArt(
            artUrl = track.artworkUrl,
            size = 44.dp,
            cornerRadius = 6.dp
        )

        // Track info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Remove button
        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove from queue",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
