package com.plexglassplayer.feature.playback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plexglassplayer.core.model.QueueItem
import com.plexglassplayer.core.model.RepeatMode
import com.plexglassplayer.core.ui.components.AlbumArt
import kotlinx.coroutines.delay

// --- REFINED PALETTE ---
private val DeepBackground = Color(0xFF050505)
private val GlowCyan = Color(0xFF00E5FF).copy(alpha = 0.12f)
private val GlowPurple = Color(0xFF7C4DFF).copy(alpha = 0.12f)
private val AccentMint = Color(0xFFB0F2E2)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF999999)

// Gradient for the Glass Background
private val GlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF202020).copy(alpha = 0.75f),
        Color(0xFF151515).copy(alpha = 0.85f)
    )
)

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

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // --- AMBIENT GLOW BLOBS ---
        // Top-Left Glow
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(350.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .blur(radius = 80.dp)
                .background(GlowCyan, CircleShape)
        )

        // Bottom-Right Glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(400.dp)
                .offset(x = 100.dp, y = 100.dp)
                .blur(radius = 100.dp)
                .background(GlowPurple, CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = TextPrimary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(40.dp))
            }

            // 2. NOW PLAYING CARD (Floating)
            if (currentTrack != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NowPlayingCard(
                        track = currentTrack!!,
                        isPlaying = isPlaying,
                        duration = duration,
                        currentPosition = currentPosition,
                        onPlayPause = { playbackManager.playPause() },
                        onNext = { playbackManager.playNext() },
                        onPrevious = { playbackManager.playPrevious() },
                        onSeek = { playbackManager.seekTo(it) },
                        onClick = onNowPlayingClick
                    )
                }
            }

            // 3. UP NEXT SHEET (Anchored to bottom)
            val upcomingTracks = queue.drop(currentIndex + 1)

            // Add spacing before the sheet starts
            Spacer(modifier = Modifier.height(24.dp))

            // This Box expands to fill the rest of the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (upcomingTracks.isNotEmpty()) {
                    UpcomingSongsSheet(
                        tracks = upcomingTracks,
                        startIndex = currentIndex + 1,
                        onTrackClick = { index -> playbackManager.playFromQueue(index) },
                        onRemoveClick = { index -> playbackManager.removeFromQueue(index) }
                    )
                } else {
                    // Empty state inside the sheet container
                    UpcomingSongsSheet(
                        tracks = emptyList(),
                        startIndex = 0,
                        onTrackClick = {},
                        onRemoveClick = {}
                    )
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
private fun NowPlayingCard(
    track: QueueItem, isPlaying: Boolean, duration: Long, currentPosition: Long,
    onPlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit,
    onSeek: (Long) -> Unit, onClick: () -> Unit
) {
    // Floating Card Style
    GlassSurface(
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(16.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.6f))
            ) {
                AlbumArt(track.artworkUrl, size = 110.dp, cornerRadius = 14.dp)
            }

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = AccentMint,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                // Slider
                Slider(
                    value = if (duration > 0) (currentPosition.toFloat() / duration) else 0f,
                    onValueChange = { onSeek((it * duration).toLong()) },
                    modifier = Modifier.fillMaxWidth().height(18.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentMint,
                        activeTrackColor = AccentMint,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SkipPrevious, "Prev", tint = TextPrimary.copy(alpha = 0.7f), modifier = Modifier.size(28.dp).clickable { onPrevious() })

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(10.dp, CircleShape, spotColor = AccentMint)
                            .clip(CircleShape)
                            .background(AccentMint)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(24.dp))
                    }

                    Icon(Icons.Default.SkipNext, "Next", tint = TextPrimary.copy(alpha = 0.7f), modifier = Modifier.size(28.dp).clickable { onNext() })
                }
            }
        }
    }
}

@Composable
private fun UpcomingSongsSheet(
    tracks: List<QueueItem>,
    startIndex: Int,
    onTrackClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    // Sheet Style: Rounded Top, Flat Bottom
    GlassSurface(
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header is INSIDE the sheet now
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Little handle bar for visual cue
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.TopCenter)
                        .offset(y = (-12).dp)
                )

                Text(
                    text = "UP NEXT",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                    color = TextSecondary
                )
            }

            if (tracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Queue is empty", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp) // Add padding at bottom for nav bar
                ) {
                    itemsIndexed(tracks) { relativeIndex, track ->
                        UpcomingTrackRow(
                            track = track,
                            position = relativeIndex + 1,
                            isLastItem = relativeIndex == tracks.lastIndex,
                            onClick = { onTrackClick(startIndex + relativeIndex) },
                            onRemove = { onRemoveClick(startIndex + relativeIndex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingTrackRow(
    track: QueueItem, position: Int, isLastItem: Boolean,
    onClick: () -> Unit, onRemove: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                position.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.width(28.dp)
            )

            AlbumArt(track.artworkUrl, size = 48.dp, cornerRadius = 10.dp)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary
                )
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "Remove", tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            }
        }

        // Separator
        if (!isLastItem) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 116.dp, end = 24.dp),
                thickness = 0.5.dp,
                color = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}

// --- FIXED GLASS SURFACE COMPONENT ---
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape,
    content: @Composable () -> Unit
) {
    // Define the border separately to avoid the error
    val borderStroke = BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.02f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )

    Box(
        modifier = modifier
            .background(GlassGradient, shape)
            .border(border = borderStroke, shape = shape)
            .clip(shape)
    ) {
        content()
    }
}