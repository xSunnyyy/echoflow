package com.plexglassplayer.feature.playback

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    // Sync position with player when not dragging
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!isDragging) {
                currentPosition = playbackManager.getCurrentPosition()
            }
            delay(500)
        }
    }

    if (currentTrack == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black))
        return
    }

    val track = currentTrack!!

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Background ---
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.artworkUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(60.dp)
        )

        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Black.copy(0.4f), Color.Black.copy(0.9f)))
        ))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    IconButton(onClick = onQueueClick) {
                        Icon(Icons.Default.MoreVert, "Queue", tint = Color.White)
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- 1. Artwork Pill ---
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.artworkUrl)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(percent = 50))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 2. Functional Seek Bar (Below Album Image) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ArcProgressBar(
                        position = currentPosition,
                        duration = duration,
                        onSeek = { seekPos ->
                            currentPosition = seekPos
                            playbackManager.seekTo(seekPos)
                        },
                        onDragging = { isDragging = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- 3. Song Info ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatTime(currentPosition),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                // --- 4. Controls ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { playbackManager.toggleShuffle() }) {
                        Icon(Icons.Default.SyncAlt, null, tint = if (isShuffleOn) Color.White else Color.White.copy(0.3f))
                    }
                    IconButton(onClick = { playbackManager.playPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White).clickable { playbackManager.playPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(40.dp))
                    }
                    IconButton(onClick = { playbackManager.playNext() }) {
                        Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = { playbackManager.toggleRepeatMode() }) {
                        val icon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                        Icon(icon, null, tint = if (repeatMode != RepeatMode.OFF) Color.White else Color.White.copy(0.3f))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
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

    // Geometry Constants
    val startAngle = 145f // Left side
    val sweepAngle = -110f // Counter-clockwise to Right side

    Canvas(modifier = modifier
        .pointerInput(duration) {
            detectTapGestures { offset ->
                val seekPos = calculateSeekFromOffset(offset, size.width, size.height, startAngle, sweepAngle, duration)
                if (seekPos != -1L) onSeek(seekPos)
            }
        }
        .pointerInput(duration) {
            detectDragGestures(
                onDragStart = { onDragging(true) },
                onDragEnd = { onDragging(false) },
                onDragCancel = { onDragging(false) },
                onDrag = { change, _ ->
                    val seekPos = calculateSeekFromOffset(change.position, size.width, size.height, startAngle, sweepAngle, duration)
                    if (seekPos != -1L) onSeek(seekPos)
                }
            )
        }
    ) {
        val strokeWidth = 5.dp.toPx()
        val w = size.width
        val h = size.height

        val arcDiameter = w * 0.75f
        val arcRadius = arcDiameter / 2

        // Lift the arc higher so it sits under the artwork and above the title
        val topLeftOffset = Offset(
            x = (w - arcDiameter) / 2,
            y = strokeWidth // Align near the top of this Box
        )

        // Track
        drawArc(
            color = Color.White.copy(alpha = 0.15f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeftOffset,
            size = Size(arcDiameter, arcDiameter),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )

        // Progress
        drawArc(
            color = Color.White,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            topLeft = topLeftOffset,
            size = Size(arcDiameter, arcDiameter),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )

        // Thumb Dot
        val currentAngle = startAngle + (sweepAngle * progress)
        val angleRad = Math.toRadians(currentAngle.toDouble())
        val centerX = w / 2
        val centerY = topLeftOffset.y + arcRadius
        val thumbX = centerX + (arcRadius * cos(angleRad)).toFloat()
        val thumbY = centerY + (arcRadius * sin(angleRad)).toFloat()

        drawCircle(Color.Black, 11.dp.toPx(), Offset(thumbX, thumbY))
        drawCircle(Color.White, 7.dp.toPx(), Offset(thumbX, thumbY))
    }
}

private fun calculateSeekFromOffset(
    offset: Offset,
    width: Int,
    height: Int,
    startAngle: Float,
    sweepAngle: Float,
    duration: Long
): Long {
    val arcDiameter = width * 0.75f
    val arcRadius = arcDiameter / 2
    val centerX = width / 2f
    val centerY = 5.dp.value + arcRadius // Theoretical center of the circle

    val dx = offset.x - centerX
    val dy = offset.y - centerY

    // Check if touch is near the arc radius
    val distanceFromCenter = sqrt(dx * dx + dy * dy)
    if (distanceFromCenter < arcRadius - 50f || distanceFromCenter > arcRadius + 50f) return -1L

    var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (touchAngle < 0) touchAngle += 360f

    // startAngle is 145, endAngle is 145 - 110 = 35
    // Normalize touchAngle to progress
    val progress = (touchAngle - startAngle) / sweepAngle
    return if (progress in 0f..1f) (progress * duration).toLong() else -1L
}

private fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}