package com.plexglassplayer.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.ui.components.*
import com.plexglassplayer.core.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = Color.Black,
        modifier = modifier
    ) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()
        val startPadding = paddingValues.calculateStartPadding(layoutDirection)
        val endPadding = paddingValues.calculateEndPadding(layoutDirection)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF121212), Color.Black)))
        ) {
            when (val state = uiState) {
                is TrackListUiState.Loading -> LoadingState(message = "Loading...")
                is TrackListUiState.Success -> {
                    val sortedTracks = remember(state.tracks) {
                        state.tracks.sortedBy { it.title.lowercase() }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = startPadding + 16.dp,
                            end = endPadding + 16.dp,
                            top = 0.dp,
                            bottom = bottomPadding + 16.dp
                        )
                    ) {
                        // --- HEADER ("My Music") ---
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onBackClick) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                                }
                                Text(
                                    text = "My Music",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // --- PLAY / SHUFFLE BUTTONS ---
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.playAll() },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Play", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.shuffleAll() },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282828)),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Icon(Icons.Default.Shuffle, null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Shuffle", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // --- TRACK LIST ---
                        itemsIndexed(sortedTracks) { _, track ->
                            TrackItem(
                                track = track,
                                onClick = { viewModel.playTrack(track) }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
                is TrackListUiState.Empty -> EmptyState(message = "No tracks found")
                is TrackListUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadTracks(true) })
            }
        }
    }
}

@Composable
private fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // --- 1. DYNAMIC BLURRED BACKGROUND ---
        AsyncImage(
            model = track.artUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
        )

        // --- 2. LOWER INTENSITY SOFT GRADIENT ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0.0f to Color.Black.copy(alpha = 0.5f),
                        0.4f to Color.Black.copy(alpha = 0.75f),
                        0.7f to Color.Black.copy(alpha = 0.9f),
                        1.0f to Color.Black.copy(alpha = 0.98f)
                    )
                )
        )

        // --- 3. GLASS OVERLAY ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.05f))
        )

        // --- 4. CONTENT ---
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.artUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.DarkGray)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = track.durationMs.formatDuration(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}