package com.plexglassplayer.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.PlaybackRepository
import com.plexglassplayer.domain.usecase.SearchTracksUseCase
import com.plexglassplayer.feature.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val playbackRepository: PlaybackRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // Debounce search queries
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _uiState.value = SearchUiState.Idle
                } else {
                    search(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _uiState.value = SearchUiState.Searching
        }
    }

    fun clearQuery() {
        _searchQuery.value = ""
        _uiState.value = SearchUiState.Idle
    }

    private fun search(query: String) {
        viewModelScope.launch {
            when (val result = searchTracksUseCase(query)) {
                is Result.Success -> {
                    val tracks = result.data
                    if (tracks.isEmpty()) {
                        _uiState.value = SearchUiState.NoResults(query)
                    } else {
                        _uiState.value = SearchUiState.Success(tracks)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Search failed")
                    _uiState.value = SearchUiState.Error(
                        result.exception.message ?: "Search failed"
                    )
                }
                else -> {}
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is SearchUiState.Success) {
                    val queueItems = playbackRepository.convertTracksToQueue(state.results)
                    val startIndex = state.results.indexOfFirst { it.id == track.id }
                    playbackManager.playTracks(queueItems, startIndex)
                    Timber.d("Started playback: ${track.title}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start playback")
            }
        }
    }
}

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Searching : SearchUiState()
    data class Success(val results: List<Track>) : SearchUiState()
    data class NoResults(val query: String) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
