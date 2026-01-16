package com.plexglassplayer.feature.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.plexglassplayer.core.ui.components.GlassCard
import com.plexglassplayer.core.util.formatDuration
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    playbackManager: PlaybackManager
) {
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val duration by playbackManager.duration.collectAsState()

    var currentPosition by remember { mutableLongStateOf(0L) }

    // Update position every second while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Blurred background from album art
        currentTrack?.artworkUrl?.let { artUrl ->
            AsyncImage(
                model = artUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                    )

                IconButton(onClick = { /* TODO: More options */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        "More",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Album artwork
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = currentTrack?.artworkUrl,
                    contentDescription = "Album artwork",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Track info
            Text(
                text = currentTrack?.title ?: "No track playing",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentTrack?.artist ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentTrack?.album ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress bar
            Column {
                Slider(
                    value = if (duration > 0) (currentPosition.toFloat() / duration) else 0f,
                    onValueChange = { value ->
                        playbackManager.seekTo((value * duration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentPosition.formatDuration(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = duration.formatDuration(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { /* TODO: Toggle shuffle */ }) {
                    Icon(
                        Icons.Default.Shuffle,
                        "Shuffle",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Previous
                IconButton(onClick = { playbackManager.playPrevious() }) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Play/Pause
                FloatingActionButton(
                    onClick = { playbackManager.playPause() },
                    modifier = Modifier.size(72.dp),
                    containerColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Next
                IconButton(onClick = { playbackManager.playNext() }) {
                    Icon(
                        Icons.Default.SkipNext,
                        "Next",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Repeat
                IconButton(onClick = { /* TODO: Toggle repeat */ }) {
                    Icon(
                        Icons.Default.Repeat,
                        "Repeat",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
