package com.plexglassplayer.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plexglassplayer.core.model.Artist
import com.plexglassplayer.core.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistListScreen(
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Load more when scrolling near the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val state = uiState
                if (state is ArtistListUiState.Success && lastVisibleIndex != null) {
                    val totalItems = state.artists.size
                    if (lastVisibleIndex >= totalItems - 10 && state.hasMore) {
                        viewModel.loadMore()
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artists") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ArtistListUiState.Loading -> {
                    LoadingState(message = "Loading artists...")
                }

                is ArtistListUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.artists) { artist ->
                            ArtistItem(
                                artist = artist,
                                onClick = { onArtistClick(artist) }
                            )
                        }

                        if (state.hasMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }

                is ArtistListUiState.Empty -> {
                    EmptyState(message = "No artists found in your library")
                }

                is ArtistListUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadArtists(isRefresh = true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistItem(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArt(
                artUrl = artist.thumbUrl ?: artist.artUrl,
                size = 56.dp,
                cornerRadius = 28.dp // Circular for artists
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
