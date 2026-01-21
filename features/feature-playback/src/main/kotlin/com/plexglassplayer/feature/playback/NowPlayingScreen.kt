package com.plexglassplayer.feature.playback

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.plexglassplayer.core.model.RepeatMode
import com.plexglassplayer.core.util.formatDuration
import kotlinx.coroutines.delay

// --- PALETTE ---
private val TextPrimary = Color.White
private val TextSecondary = Color.White.copy(alpha = 0.7f)
private val TextTertiary = Color.White.copy(alpha = 0.5f)
private val PlayButtonColor = Color(0xFF7CA0C0)
private val ActiveIconColor = Color(0xFF7CA0C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
    playbackManager: PlaybackManager
) {
    val context = LocalContext.current
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val duration by playbackManager.duration.collectAsState()
    val shuffleEnabled by playbackManager.shuffleEnabled.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()

    var currentPosition by remember { mutableLongStateOf(0L) }

    // --- VOLUME LOGIC ---
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // --- 1. BACKGROUND ---
        currentTrack?.artworkUrl?.let { artUrl ->
            AsyncImage(
                model = artUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
            // Removed horizontal padding from parent Column to allow full-width control in children
        ) {

            // --- 2. HEADER (MATCHED TO QUEUE SCREEN) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp), // Matched Padding
                contentAlignment = Alignment.Center
            ) {
                // Back Button (Matched Style: Circle Background)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Title (Matched Typography: Black weight, 2.sp spacing)
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = TextPrimary.copy(alpha = 0.9f)
                )

                // Queue Button (Plain, aligned end)
                IconButton(
                    onClick = onQueueClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp) // Matched size for symmetry
                ) {
                    Icon(Icons.Default.QueueMusic, contentDescription = "Queue", tint = TextPrimary)
                }
            }

            // Wrapper for remaining content to apply the standard horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp), // Restored padding for body content
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3. ALBUM ART ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(32.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.8f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.DarkGray)
                ) {
                    AsyncImage(
                        model = currentTrack?.artworkUrl,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // --- 4. SEEK BAR ---
                Spacer(modifier = Modifier.height(16.dp))

                GlowingSeekBar(
                    position = currentPosition,
                    duration = duration,
                    onSeek = { newPos -> playbackManager.seekTo(newPos) }
                )

                // Timestamps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentPosition.formatDuration(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = duration.formatDuration(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- 5. TEXT INFO ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTrack?.title ?: "Not Playing",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentTrack?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentTrack?.album ?: "Unknown Album",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 6. CONTROLS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { playbackManager.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffleEnabled) ActiveIconColor else TextTertiary
                        )
                    }

                    IconButton(onClick = { playbackManager.playPrevious() }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Prev", tint = TextPrimary, modifier = Modifier.size(32.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = PlayButtonColor.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(PlayButtonColor)
                            .clickable { playbackManager.playPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = { playbackManager.playNext() }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipNext, "Next", tint = TextPrimary, modifier = Modifier.size(32.dp))
                    }

                    IconButton(onClick = { playbackManager.toggleRepeatMode() }) {
                        val repeatIcon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Rounded.Replay
                        val repeatTint = if (repeatMode != RepeatMode.OFF) ActiveIconColor else TextTertiary
                        Icon(imageVector = repeatIcon, contentDescription = "Repeat", tint = repeatTint)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- 7. VOLUME ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeDown, "Vol Low", tint = TextTertiary, modifier = Modifier.size(20.dp))

                    Slider(
                        value = currentVolume,
                        valueRange = 0f..maxVolume,
                        onValueChange = { newVolume ->
                            currentVolume = newVolume
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.toInt(), 0)
                        },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = TextSecondary,
                            activeTrackColor = TextSecondary,
                            inactiveTrackColor = TextSecondary.copy(alpha = 0.1f)
                        )
                    )

                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Vol High", tint = TextTertiary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/**
 * Thinner "Glowing" Seek Bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlowingSeekBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = if (duration > 0) position.toFloat() / duration else 0f,
        onValueChange = { onSeek((it * duration).toLong()) },
        modifier = modifier.fillMaxWidth(),
        thumb = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = PlayButtonColor,
                        ambientColor = PlayButtonColor
                    )
                    .background(PlayButtonColor, CircleShape)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = SliderDefaults.colors(
                    activeTrackColor = PlayButtonColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                    thumbColor = PlayButtonColor
                ),
                modifier = Modifier.height(2.dp)
            )
        }
    )
}