package com.plexglassplayer.feature.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.plexglassplayer.core.util.formatDuration
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
    playbackManager: PlaybackManager
) {
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val duration by playbackManager.duration.collectAsState()
    val shuffleEnabled by playbackManager.shuffleEnabled.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()

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
                    .blur(80.dp),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for better readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6B4E71).copy(alpha = 0.85f),
                                Color(0xFF4A3A4D).copy(alpha = 0.95f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                // Invisible spacer to center the title
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Album artwork - large and centered
            AsyncImage(
                model = currentTrack?.artworkUrl,
                contentDescription = "Album artwork",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Track info
            Text(
                text = currentTrack?.title ?: "No track playing",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentTrack?.artist ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (duration > 0) (currentPosition.toFloat() / duration) else 0f,
                    onValueChange = { value ->
                        playbackManager.seekTo((value * duration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentPosition.formatDuration(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = duration.formatDuration(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous
                IconButton(
                    onClick = { playbackManager.playPrevious() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        "Previous",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Play/Pause - large circular button with light color
                Surface(
                    onClick = { playbackManager.playPause() },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFB8D8D8) // Light blue/mint color
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color(0xFF2C2C3A),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { playbackManager.playNext() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        "Next",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { playbackManager.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle,
                        "Shuffle",
                        tint = if (shuffleEnabled) {
                            Color.White
                        } else {
                            Color.White.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Repeat
                IconButton(onClick = { playbackManager.toggleRepeatMode() }) {
                    Icon(
                        imageVector = when (repeatMode) {
                            com.plexglassplayer.core.model.RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = when (repeatMode) {
                            com.plexglassplayer.core.model.RepeatMode.OFF -> Color.White.copy(alpha = 0.4f)
                            else -> Color.White
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Lyrics/Comments
                IconButton(onClick = { /* TODO: Show lyrics */ }) {
                    Icon(
                        Icons.Default.Chat,
                        "Lyrics",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Cast/AirPlay
                IconButton(onClick = { /* TODO: Show cast options */ }) {
                    Icon(
                        Icons.Default.Cast,
                        "Cast",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
