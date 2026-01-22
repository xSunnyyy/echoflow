package com.plexglassplayer.feature.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.plexglassplayer.core.model.Playlist
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.ui.components.*
import kotlinx.coroutines.delay
import java.util.Calendar

// --- PALETTE ---
private val AccentMint = Color(0xFFB0F2E2)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFE0E0E0)

// --- GRADIENTS ---
private val CardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF202020).copy(alpha = 0.60f),
        Color(0xFF050505).copy(alpha = 0.70f)
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

private val BackgroundScrim = Brush.verticalGradient(
    colors = listOf(
        Color.Black.copy(alpha = 0.4f),
        Color.Black.copy(alpha = 0.95f)
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onLibraryClick: () -> Unit,
    onSeeAllMusicClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNowPlayingClick: () -> Unit = {},
    onClosePlayer: () -> Unit = {
        try { viewModel.playbackManager.clearQueue() } catch (e: Exception) { }
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val playbackManager = viewModel.playbackManager
    val currentTrack by playbackManager.currentTrack.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val duration by playbackManager.duration.collectAsState()
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    var currentPosition by remember { mutableLongStateOf(0L) }

    // --- DIALOG STATES ---
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Deletion States
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var trackToRemove by remember { mutableStateOf<Track?>(null) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = playbackManager.getCurrentPosition()
            delay(1000)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // --- BACKGROUND ---
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505)))

        if (currentTrack != null) {
            Crossfade(targetState = currentTrack!!.artworkUrl, label = "HomeBg") { url ->
                if (url != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.8f)
                            .blur(50.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(BackgroundScrim))

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentTrack != null) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue != SwipeToDismissBoxValue.Settled) {
                                onClosePlayer()
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        content = {
                            GlassMiniPlayer(
                                track = currentTrack!!,
                                isPlaying = isPlaying,
                                currentPosition = currentPosition,
                                duration = duration,
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

            // --- PULL TO REFRESH BOX ---
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    top = 4.dp,
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding()
                )
            ) {
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentMint)
                        }
                    }
                    is HomeUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 0.dp,
                                bottom = 24.dp
                            )
                        ) {
                            // 1. HEADER
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 8.dp, top = 0.dp, bottom = 0.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Home",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    IconButton(onClick = onSettingsClick) {
                                        Icon(Icons.Default.Settings, "Settings", tint = TextPrimary)
                                    }
                                }
                            }

                            // 2. GREETING
                            item {
                                Text(
                                    text = "${getGreeting()}, $userName",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(start = 20.dp, bottom = 24.dp)
                                )
                            }

                            // 3. RECENTLY PLAYED
                            item {
                                Text(
                                    text = "Recently Played",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(state.recentTracks) { track ->
                                        RecentlyPlayedCard(
                                            track = track,
                                            onClick = { viewModel.playTrack(track) }
                                        )
                                    }
                                }
                            }

                            // 4. ALL MUSIC
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "All Music",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextSecondary
                                    )

                                    TextButton(onClick = onSeeAllMusicClick) {
                                        Text(
                                            text = "See all",
                                            color = AccentMint,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                val randomTracks = remember(state.allTracks) {
                                    state.allTracks.shuffled().take(10)
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(randomTracks) { track ->
                                        RecentlyPlayedCard(
                                            track = track,
                                            onClick = { viewModel.playTrack(track) }
                                        )
                                    }
                                }
                            }

                            // 5. YOUR PLAYLISTS
                            item {
                                Spacer(modifier = Modifier.height(32.dp))

                                // Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Playlists",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                        color = TextSecondary
                                    )

                                    // "New" Pill Button
                                    Surface(
                                        color = Color(0xFF1E1E1E),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .clickable { showCreatePlaylistDialog = true }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        ) {
                                            Icon(Icons.Default.Add, null, tint = AccentMint, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("New", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (state.playlists.isEmpty()) {
                                    Text(
                                        "No playlists found",
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        color = TextSecondary.copy(0.5f)
                                    )
                                } else {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(state.playlists) { playlist ->
                                            MixCard(
                                                title = playlist.title,
                                                subtitle = "${playlist.trackCount} Songs",
                                                onClick = { viewModel.openPlaylist(playlist) },
                                                onLongClick = { playlistToDelete = playlist } // Triggers Delete Dialog
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is HomeUiState.Empty -> { /* Handle Empty */ }
                    is HomeUiState.Error -> { /* Handle Error */ }
                }
            }
        }

        // --- CREATE PLAYLIST DIALOG ---
        if (showCreatePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("New Playlist") },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
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
                                viewModel.createPlaylist(newPlaylistName)
                                showCreatePlaylistDialog = false
                                newPlaylistName = ""
                            }
                        }
                    ) { Text("Create", color = AccentMint) }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                textContentColor = TextSecondary,
                titleContentColor = TextPrimary
            )
        }

        // --- DELETE PLAYLIST DIALOG ---
        if (playlistToDelete != null) {
            AlertDialog(
                onDismissRequest = { playlistToDelete = null },
                title = { Text("Delete Playlist?", color = TextPrimary) },
                text = {
                    Text("Are you sure you want to delete '${playlistToDelete?.title}'?", color = TextSecondary)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            playlistToDelete?.let { viewModel.deletePlaylist(it) }
                            playlistToDelete = null
                        }
                    ) { Text("Delete", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { playlistToDelete = null }) {
                        Text("Cancel", color = AccentMint)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = TextPrimary
            )
        }

        // --- REMOVE SONG FROM PLAYLIST DIALOG ---
        if (trackToRemove != null) {
            AlertDialog(
                onDismissRequest = { trackToRemove = null },
                title = { Text("Remove Song?", color = TextPrimary) },
                text = {
                    Text("Remove '${trackToRemove?.title}' from this playlist?", color = TextSecondary)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            trackToRemove?.let { viewModel.removeTrackFromPlaylist(it) }
                            trackToRemove = null
                        }
                    ) { Text("Remove", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { trackToRemove = null }) {
                        Text("Cancel", color = AccentMint)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = TextPrimary
            )
        }

        // --- PLAYLIST BOTTOM SHEET ---
        val state = uiState
        if (state is HomeUiState.Success && state.selectedPlaylist != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closePlaylistSheet() },
                containerColor = Color(0xFF121212),
                contentColor = TextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    Text(
                        text = state.selectedPlaylist.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(20.dp),
                        color = AccentMint
                    )

                    if (state.selectedPlaylistTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentMint)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            itemsIndexed(state.selectedPlaylistTracks) { index, track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { viewModel.playFromPlaylist(index) },
                                            onLongClick = { trackToRemove = track } // Long press to remove song
                                        )
                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.DarkGray)
                                    ) {
                                        AsyncImage(
                                            model = track.artUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = track.artistName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun RecentlyPlayedCard(track: Track, onClick: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.width(130.dp).clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(track.artUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = {
                    Icon(Icons.Default.BrokenImage, contentDescription = "Error", tint = Color.White.copy(0.2f))
                }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artistName,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MixCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        GlassSurface(
            shape = RoundedCornerShape(20.dp),
            gradient = CardGradient,
            modifier = Modifier.size(140.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentMint,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary
        )
    }
}

@Composable
private fun GlassMiniPlayer(
    track: com.plexglassplayer.core.model.QueueItem,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
            .height(72.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        GlassSurface(
            shape = RoundedCornerShape(24.dp),
            gradient = CardGradient,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(track.artworkUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentMint.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        IconButton(onClick = onPrevious) {
                            Icon(Icons.Default.SkipPrevious, "Previous", tint = TextPrimary)
                        }
                        IconButton(onClick = onPlayPause) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = TextPrimary
                            )
                        }
                        IconButton(onClick = onNext) {
                            Icon(Icons.Default.SkipNext, "Next", tint = TextPrimary)
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = AccentMint,
                    trackColor = Color.Transparent
                )
            }
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
    Box(
        modifier = modifier
            .background(gradient, shape)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                    )
                ),
                shape
            )
            .clip(shape)
    ) {
        content()
    }
}

private fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}