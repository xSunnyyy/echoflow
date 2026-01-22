package com.plexglassplayer.feature.playback

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plexglassplayer.core.model.RepeatMode
import com.plexglassplayer.core.util.formatDuration
import com.plexglassplayer.feature.playback.NowPlayingViewModel
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val TextPrimary = Color(0xFF0E0E10)
private val TextSecondary = Color(0xFF5A5A60)
private val TextTertiary = Color(0xFF8B8B92)
private val PlayButtonColor = Color(0xFF111113)
private val ActiveIconColor = Color(0xFF111113)
private val AccentMint = Color(0xFFB0F2E2)
private val ScreenBackground = Color(0xFFF4F4F6)
private val CardBackground = Color(0xFF1A2233)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
    playbackManager: PlaybackManager,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val duration by playbackManager.duration.collectAsState()
    val shuffleEnabled by playbackManager.shuffleEnabled.collectAsState()
    val repeatMode by playbackManager.repeatMode.collectAsState()

    // Playlist Data from ViewModel
    val playlists by viewModel.playlists.collectAsState()

    // Seekbar State
    var currentPosition by remember { mutableLongStateOf(0L) }

    // Dialog States
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Volume Logic
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVolume by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            // --- 2. HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onQueueClick,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- 3. ALBUM ART ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(0.78f)
                        .shadow(24.dp, RoundedCornerShape(80.dp), spotColor = Color.Black.copy(alpha = 0.35f))
                        .clip(RoundedCornerShape(80.dp))
                        .background(CardBackground)
                ) {
                    AsyncImage(
                        model = currentTrack?.artworkUrl,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Black.copy(alpha = 0.8f)
                                    ),
                                    startY = 0.4f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 28.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentTrack?.title ?: "Not Playing",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentTrack?.artist ?: "Unknown Artist",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                ArcSeekBar(
                    position = currentPosition,
                    duration = duration,
                    onSeek = { newPos -> playbackManager.seekTo(newPos) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )

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

                Spacer(modifier = Modifier.height(18.dp))

                // --- 6. CONTROLS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = { playbackManager.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffleEnabled) ActiveIconColor else TextTertiary
                        )
                    }

                    // Previous
                    IconButton(onClick = { playbackManager.playPrevious() }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Prev", tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }

                    // Play/Pause
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.25f))
                            .clip(CircleShape)
                            .background(PlayButtonColor)
                            .clickable { playbackManager.playPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Next
                    IconButton(onClick = { playbackManager.playNext() }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipNext, "Next", tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }

                    // Repeat
                    IconButton(onClick = { playbackManager.toggleRepeatMode() }) {
                        val repeatIcon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Rounded.Replay
                        val repeatTint = if (repeatMode != RepeatMode.OFF) ActiveIconColor else TextTertiary
                        Icon(imageVector = repeatIcon, contentDescription = "Repeat", tint = repeatTint)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 7. VOLUME & ADD TO PLAYLIST ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
                            inactiveTrackColor = TextSecondary.copy(alpha = 0.2f)
                        )
                    )

                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Vol High", tint = TextTertiary, modifier = Modifier.size(20.dp))

                    // NEW: Add to Playlist (Triggers Dialog)
                    IconButton(onClick = { showPlaylistDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add to Playlist",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // --- PLAYLIST SELECTION DIALOG ---
    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist", color = TextPrimary) },
            containerColor = Color(0xFF1E1E1E),
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    // Option to Create New
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPlaylistDialog = false
                                    showCreateDialog = true
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(AccentMint.copy(0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = AccentMint)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Create New Playlist", color = AccentMint, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = Color.White.copy(0.1f))
                    }

                    if (playlists.isEmpty()) {
                        item {
                            Text(
                                "No existing playlists.",
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }

                    // List of Existing Playlists
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentTrack?.let { queueItem ->
                                        viewModel.addToPlaylist(playlist, queueItem)
                                    }
                                    showPlaylistDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.QueueMusic, null, tint = AccentMint, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(playlist.title, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("${playlist.trackCount} tracks", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("Cancel", color = AccentMint)
                }
            }
        )
    }

    // --- CREATE NEW PLAYLIST DIALOG ---
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Playlist", color = TextPrimary) },
            containerColor = Color(0xFF1E1E1E),
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentMint,
                        focusedLabelColor = AccentMint,
                        cursorColor = AccentMint
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            currentTrack?.let { track ->
                                viewModel.createPlaylist(newPlaylistName, track)
                            }
                            showCreateDialog = false
                            newPlaylistName = ""
                        }
                    }
                ) { Text("Create", color = AccentMint) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ArcSeekBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
    val startAngle = 200f
    val sweepAngle = 140f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(duration) {
                    detectDragGestures { change, _ ->
                        val center = Offset(size.width / 2f, size.height)
                        val touch = change.position
                        val angle = Math.toDegrees(
                            atan2(
                                (touch.y - center.y).toDouble(),
                                (touch.x - center.x).toDouble()
                            )
                        ).toFloat().let { if (it < 0) it + 360f else it }

                        val endAngle = (startAngle + sweepAngle) % 360f
                        val inRange = if (startAngle < endAngle) {
                            angle in startAngle..endAngle
                        } else {
                            angle >= startAngle || angle <= endAngle
                        }

                        if (inRange && duration > 0) {
                            val clamped = if (startAngle < endAngle) {
                                angle.coerceIn(startAngle, endAngle)
                            } else {
                                if (angle >= startAngle) angle else angle + 360f
                            }
                            val normalized = (clamped - startAngle) / sweepAngle
                            val newPos = (normalized.coerceIn(0f, 1f) * duration).toLong()
                            onSeek(newPos)
                        }
                    }
                }
        ) {
            val strokeWidth = 10.dp.toPx()
            val radius = min(size.width, size.height * 2f) / 2f - strokeWidth
            val center = Offset(size.width / 2f, size.height)

            drawArc(
                color = Color.Black.copy(alpha = 0.08f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = PlayButtonColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle * progress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val theta = Math.toRadians((startAngle + sweepAngle * progress).toDouble())
            val thumbX = center.x + radius * cos(theta).toFloat()
            val thumbY = center.y + radius * sin(theta).toFloat()
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = PlayButtonColor,
                radius = 6.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        Text(
            text = duration.formatDuration(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondary
        )
    }
}
