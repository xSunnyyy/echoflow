package com.plexglassplayer.feature.playback

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.plexglassplayer.core.model.QueueItem
import com.plexglassplayer.core.model.RepeatMode
import com.plexglassplayer.core.ui.components.AlbumArt
import kotlinx.coroutines.delay

// --- PALETTE ---
private val AccentMint = Color(0xFFB0F2E2)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFE0E0E0)

// --- GLASS GRADIENTS ---
private val CardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF202020).copy(alpha = 0.60f),
        Color(0xFF050505).copy(alpha = 0.70f)
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

private val ListCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF151515).copy(alpha = 0.60f),
        Color(0xFF050505).copy(alpha = 0.85f)
    )
)

private val BackgroundScrim = Brush.verticalGradient(
    colors = listOf(
        Color.Black.copy(alpha = 0.4f),
        Color.Black.copy(alpha = 0.9f)
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
    val duration by playbackManager.duration.collectAsState()

    var currentPosition by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // --- BACKGROUND ---
        if (currentTrack != null) {
            Crossfade(targetState = currentTrack!!.artworkUrl, label = "BackgroundFade") { artworkUrl ->
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(100.dp)
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505)))
        }

        Box(modifier = Modifier.fillMaxSize().background(BackgroundScrim))

        // --- CONTENT ---
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                    color = TextPrimary.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.weight(1f))

                // TOP BAR SHUFFLE TOGGLE
                IconButton(onClick = { playbackManager.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle,
                        null,
                        tint = if (shuffleEnabled) AccentMint else TextPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. NOW PLAYING CARD
            if (currentTrack != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NowPlayingCard(
                        track = currentTrack!!,
                        isPlaying = isPlaying,
                        duration = duration,
                        currentPosition = currentPosition,
                        onPlayPause = { playbackManager.playPause() },
                        // FIX: When Shuffle is ON, we use the player's internal playNext() for true randomness.
                        // When Shuffle is OFF, we use manual index navigation to stay in order.
                        onNext = {
                            if (shuffleEnabled) {
                                playbackManager.playNext()
                            } else if (currentIndex < queue.size - 1) {
                                playbackManager.playFromQueue(currentIndex + 1)
                            }
                        },
                        onPrevious = {
                            playbackManager.playPrevious()
                        },
                        onSeek = { playbackManager.seekTo(it) },
                        onClick = onNowPlayingClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. UP NEXT CARD (Dynamic "Alive" View)
            // If Shuffle is active, we show a randomized preview of the entire library queue
            val upcomingTracks = remember(queue, currentIndex, shuffleEnabled) {
                if (shuffleEnabled) {
                    queue.filterIndexed { index, _ -> index != currentIndex }.shuffled()
                } else {
                    queue.drop(currentIndex + 1)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                UpcomingSongsList(
                    tracks = upcomingTracks,
                    startIndex = currentIndex + 1,
                    onTrackClick = { index -> playbackManager.playFromQueue(index) },
                    onRemoveClick = { index -> playbackManager.removeFromQueue(index) }
                )
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
    GlassSurface(
        shape = RoundedCornerShape(28.dp),
        gradient = CardGradient
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            ) {
                AlbumArt(track.artworkUrl, size = 110.dp, cornerRadius = 16.dp)
            }

            Column(
                modifier = Modifier.weight(1f).height(110.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(track.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(track.artist, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = AccentMint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Slider(
                    value = if (duration > 0) (currentPosition.toFloat() / duration) else 0f,
                    onValueChange = { onSeek((it * duration).toLong()) },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    colors = SliderDefaults.colors(thumbColor = AccentMint, activeTrackColor = AccentMint, inactiveTrackColor = Color.White.copy(alpha = 0.15f))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SkipPrevious, "Prev", tint = TextPrimary.copy(alpha = 0.8f), modifier = Modifier.size(28.dp).clickable { onPrevious() })

                    Box(
                        modifier = Modifier.size(44.dp).shadow(8.dp, CircleShape, spotColor = AccentMint).clip(CircleShape).background(AccentMint).clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(24.dp))
                    }

                    Icon(Icons.Default.SkipNext, "Next", tint = TextPrimary.copy(alpha = 0.8f), modifier = Modifier.size(28.dp).clickable { onNext() })
                }
            }
        }
    }
}

@Composable
private fun UpcomingSongsList(
    tracks: List<QueueItem>,
    startIndex: Int,
    onTrackClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    GlassSurface(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxSize(),
        gradient = ListCardGradient
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UP NEXT",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${tracks.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }

            if (tracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Queue is empty", color = TextSecondary.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
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
                text = position.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.width(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "Remove", tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }

        if (!isLastItem) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp, end = 24.dp),
                thickness = 0.5.dp,
                color = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape,
    gradient: Brush,
    content: @Composable () -> Unit
) {
    val borderStroke = BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)),
            start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )

    Box(
        modifier = modifier
            .background(gradient, shape)
            .border(border = borderStroke, shape = shape)
            .clip(shape)
    ) {
        content()
    }
}