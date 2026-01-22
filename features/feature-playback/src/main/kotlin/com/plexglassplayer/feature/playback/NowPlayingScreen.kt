package com.plexglassplayer.feature.playback

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Fixed 'sp' error
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.plexglassplayer.core.model.RepeatMode
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.math.*

@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val playbackManager = viewModel.playbackManager
    val currentTrack by playbackManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by playbackManager.isPlaying.collectAsStateWithLifecycle()
    val duration by playbackManager.duration.collectAsStateWithLifecycle()
    val isShuffleOn by playbackManager.shuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by playbackManager.repeatMode.collectAsStateWithLifecycle(initialValue = RepeatMode.OFF)

    var currentPosition by remember { mutableLongStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!isDragging) { currentPosition = playbackManager.getCurrentPosition() }
            delay(500)
        }
    }

    if (currentTrack == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }

    val track = currentTrack!!

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. Background Blur Layer ---
        AnimatedContent(
            targetState = track.artworkUrl,
            transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) },
            label = "BackgroundFade"
        ) { artworkUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(artworkUrl).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(60.dp)
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Black.copy(0.5f), Color.Black.copy(0.95f)))
        ))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    IconButton(onClick = onQueueClick) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val pillWidthFraction = 0.80f

                        // Artwork Pill with Fade Transition
                        AnimatedContent(
                            targetState = track.artworkUrl,
                            transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
                            label = "ArtworkFade"
                        ) { artworkUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(artworkUrl).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth(pillWidthFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(bottomStartPercent = 50, bottomEndPercent = 50))
                            )
                        }

                        // Arc Seeker
                        ArcProgressBar(
                            position = currentPosition,
                            duration = duration,
                            onSeek = { currentPosition = it; playbackManager.seekTo(it) },
                            onDragging = { isDragging = it },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(pillWidthFraction + 0.04f)
                                .aspectRatio(1f)
                                .offset(y = 18.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(start = 24.dp, top = 84.dp, end = 24.dp)
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formatTime(currentPosition),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                // Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { playbackManager.toggleShuffle() }) {
                        Icon(Icons.Default.SyncAlt, null, tint = if (isShuffleOn) Color.White else Color.White.copy(0.3f))
                    }
                    IconButton(onClick = { playbackManager.playPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(42.dp))
                    }
                    Box(
                        modifier = Modifier.size(76.dp).clip(CircleShape).background(Color.White).clickable { playbackManager.playPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(44.dp))
                    }
                    IconButton(onClick = { playbackManager.playNext() }) {
                        Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(42.dp))
                    }
                    IconButton(onClick = { playbackManager.toggleRepeatMode() }) {
                        val icon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                        Icon(icon, null, tint = if (repeatMode != RepeatMode.OFF) Color.White else Color.White.copy(0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun ArcProgressBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onDragging: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
    val startAngle = 170f
    val sweepAngle = -160f

    Canvas(modifier = modifier
        .pointerInput(duration) {
            detectDragGestures(
                onDragStart = { onDragging(true) },
                onDragEnd = { onDragging(false) },
                onDragCancel = { onDragging(false) },
                onDrag = { change, _ ->
                    // FIX: Convert Int width/height to Float
                    val seekPos = calculateSeekFromOffset(
                        offset = change.position,
                        width = size.width.toFloat(),
                        height = size.height.toFloat(),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        duration = duration
                    )
                    if (seekPos != -1L) onSeek(seekPos)
                }
            )
        }
    ) {
        val strokeWidth = 3.dp.toPx()
        val arcDiameter = size.width * 0.96f
        val arcRadius = arcDiameter / 2
        val arcSize = Size(arcDiameter, arcDiameter)

        val topLeftOffset = Offset(
            x = (size.width - arcDiameter) / 2,
            y = (size.height - arcDiameter) / 2
        )

        // Drop Shadow
        drawArc(
            color = Color.Black.copy(alpha = 0.25f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeftOffset.copy(y = topLeftOffset.y + 2.dp.toPx()),
            size = arcSize,
            style = Stroke(strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Background Track
        drawArc(
            color = Color.White.copy(alpha = 0.15f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeftOffset,
            size = arcSize,
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )

        // Progress Track
        drawArc(
            color = Color.White,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            topLeft = topLeftOffset,
            size = arcSize,
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )

        // Thumb
        val currentAngle = startAngle + (sweepAngle * progress)
        val angleRad = Math.toRadians(currentAngle.toDouble())
        val centerX = size.width / 2
        val centerY = size.height / 2
        val thumbX = centerX + (arcRadius * cos(angleRad)).toFloat()
        val thumbY = centerY + (arcRadius * sin(angleRad)).toFloat()

        drawCircle(Color.White, 9.dp.toPx(), Offset(thumbX, thumbY))
        drawCircle(Color.Black, 6.dp.toPx(), Offset(thumbX, thumbY))
    }
}

private fun calculateSeekFromOffset(
    offset: Offset,
    width: Float,
    height: Float,
    startAngle: Float,
    sweepAngle: Float,
    duration: Long
): Long {
    val centerX = width / 2f
    val centerY = height / 2f
    val dx = offset.x - centerX
    val dy = offset.y - centerY

    val distanceFromCenter = sqrt(dx * dx + dy * dy)
    if (distanceFromCenter < (width / 2) - 120f || distanceFromCenter > (width / 2) + 120f) return -1L

    var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (touchAngle < 0) touchAngle += 360f

    var delta = touchAngle - startAngle
    while (delta > 180f) delta -= 360f
    while (delta < -180f) delta += 360f

    val progress = delta / sweepAngle

    return if (progress in 0f..1f) (progress * duration).toLong() else -1L
}

private fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}