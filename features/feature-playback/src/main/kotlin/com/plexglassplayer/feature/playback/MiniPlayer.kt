package com.plexglassplayer.feature.playback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plexglassplayer.core.ui.components.AlbumArt
import com.plexglassplayer.core.ui.components.GlassCard
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    playbackManager: PlaybackManager,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val duration by playbackManager.duration.collectAsState()

    var currentPosition by remember { mutableLongStateOf(0L) }

    // Update position while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f

    AnimatedVisibility(
        visible = currentTrack != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Column {
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
            )

            // Mini player content
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandClick)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art
                    AlbumArt(
                        artUrl = currentTrack?.artworkUrl,
                        size = 48.dp,
                        cornerRadius = 4.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Track info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentTrack?.title ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = currentTrack?.artist ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Play/Pause button
                    IconButton(
                        onClick = { playbackManager.playPause() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }

                    // Next button
                    IconButton(
                        onClick = { playbackManager.playNext() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next"
                        )
                    }
                }
            }
        }
    }
}
