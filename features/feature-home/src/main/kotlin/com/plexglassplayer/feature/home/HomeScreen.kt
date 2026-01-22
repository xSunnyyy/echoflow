package com.plexglassplayer.feature.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.plexglassplayer.core.model.Playlist
import com.plexglassplayer.core.model.Track
import kotlinx.coroutines.delay
import java.util.Calendar

// --- LIQUID PALETTE ---
private val AccentMint = Color(0xFFB0F2E2)
private val GlassWhite = Color.White.copy(alpha = 0.05f)
private val TextPrimary = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onLibraryClick: () -> Unit,
    onSeeAllMusicClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNowPlayingClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onClosePlayer: () -> Unit = {
        try { viewModel.playbackManager.clearQueue() } catch (e: Exception) { }
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val playbackManager = viewModel.playbackManager
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    // DIALOG STATE
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    var currentPosition by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF020202))) {
        if (currentTrack != null) {
            Crossfade(targetState = currentTrack!!.artworkUrl, label = "LiquidBg") { url ->
                AsyncImage(
                    model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.4f).blur(100.dp)
                )
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentTrack != null) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue != SwipeToDismissBoxValue.Settled) {
                                onClosePlayer()
                                true
                            } else { false }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        content = {
                            FrostedCapsulePlayer(
                                track = currentTrack!!,
                                isPlaying = isPlaying,
                                onPlayPause = { playbackManager.playPause() },
                                onNext = { playbackManager.playNext() },
                                onPrevious = { playbackManager.playPrevious() },
                                onClick = onNowPlayingClick
                            )
                        }
                    )
                }
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is HomeUiState.Success -> {
                    // LIMITED TO 5 FOR QUICK SCROLLING
                    val limitedRandomTracks = remember(state.allTracks) {
                        state.allTracks.shuffled().take(5)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(
                            start = paddingValues.calculateStartPadding(layoutDirection),
                            top = 0.dp, // NO TOP PADDING
                            end = paddingValues.calculateEndPadding(layoutDirection)
                        ),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 24.dp)) {
                                Text(
                                    text = getGreeting().uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 5.sp),
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary
                                )
                            }
                        }

                        item {
                            SectionHeaderClean("RECENTLY PLAYED")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                items(state.recentTracks) { track ->
                                    LiquidTrackCard(track, onClick = { viewModel.playTrack(track) })
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(32.dp))
                            SectionHeaderWithAction("ALL MUSIC", onSeeAllMusicClick)
                        }

                        items(limitedRandomTracks) { track ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                CleanTrackRow(track, onClick = { viewModel.playTrack(track) })
                            }
                        }

                        // PLAYLIST SECTION WITH ADD BUTTON
                        item {
                            Spacer(Modifier.height(32.dp))
                            SectionHeaderWithAction(
                                title = "YOUR PLAYLISTS",
                                actionLabel = "NEW",
                                onAction = { showCreatePlaylistDialog = true }
                            )
                            if (state.playlists.isEmpty()) {
                                Text("No playlists found", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White.copy(0.3f))
                            } else {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    items(state.playlists) { playlist ->
                                        LiquidPlaylistCard(playlist = playlist, onClick = { viewModel.openPlaylist(playlist) })
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // CREATE PLAYLIST DIALOG
        if (showCreatePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("New Playlist", color = TextPrimary) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentMint, cursorColor = AccentMint)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            showCreatePlaylistDialog = false
                            newPlaylistName = ""
                        }
                    }) { Text("Create", color = AccentMint) }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) { Text("Cancel", color = Color.White.copy(0.6f)) }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

// --- COMPONENTS ---

@Composable
private fun LiquidPlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier.size(140.dp).clip(RoundedCornerShape(28.dp)).background(GlassWhite)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LibraryMusic, null, tint = AccentMint.copy(0.4f), modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(playlist.title, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
        Text("${playlist.trackCount} Tracks", style = MaterialTheme.typography.bodySmall, color = AccentMint)
    }
}

@Composable
private fun FrostedCapsulePlayer(
    track: com.plexglassplayer.core.model.QueueItem, isPlaying: Boolean,
    onPlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onClick: () -> Unit
) {
    Box(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp).fillMaxWidth().height(84.dp)
            .shadow(24.dp, RoundedCornerShape(42.dp))
            .clickable { onClick() }
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(42.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(42.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = track.artworkUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(60.dp).clip(CircleShape))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(track.artist, style = MaterialTheme.typography.bodySmall, color = AccentMint, maxLines = 1)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, null, tint = TextPrimary) }
                IconButton(onClick = onPlayPause, modifier = Modifier.background(AccentMint, CircleShape).size(48.dp)) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.Black)
                }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, null, tint = TextPrimary) }
            }
        }
    }
}

@Composable
private fun LiquidTrackCard(track: Track, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable { onClick() }) {
        Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(28.dp)).background(GlassWhite)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(28.dp))) {
            AsyncImage(model = track.artUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(8.dp))
        Text(track.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary, maxLines = 1)
    }
}

@Composable
private fun CleanTrackRow(track: Track, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = track.artUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(GlassWhite))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary, maxLines = 1)
            Text(track.artistName, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), maxLines = 1)
        }
    }
}

@Composable
private fun SectionHeaderClean(title: String) {
    Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(start = 24.dp, bottom = 12.dp))
}

@Composable
private fun SectionHeaderWithAction(title: String, onAction: () -> Unit, actionLabel: String = "SEE ALL") {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = Color.White.copy(alpha = 0.4f))
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color.White.copy(0.1f)),
            modifier = Modifier.width(80.dp).height(32.dp).clickable { onAction() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = AccentMint)
            }
        }
    }
}

private fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}