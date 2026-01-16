package com.plexglassplayer.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.domain.usecase.GetTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: String = savedStateHandle["albumId"]
        ?: throw IllegalArgumentException("albumId is required")

    private val _uiState = MutableStateFlow<TrackListUiState>(TrackListUiState.Loading)
    val uiState: StateFlow<TrackListUiState> = _uiState.asStateFlow()

    init {
        loadTracks()
    }

    fun loadTracks(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = TrackListUiState.Loading

            when (val result = getTracksUseCase(albumId = albumId)) {
                is Result.Success -> {
                    val tracks = result.data
                    if (tracks.isEmpty()) {
                        _uiState.value = TrackListUiState.Empty
                    } else {
                        _uiState.value = TrackListUiState.Success(tracks)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to load tracks")
                    _uiState.value = TrackListUiState.Error(
                        result.exception.message ?: "Failed to load tracks"
                    )
                }
                else -> {}
            }
        }
    }

    fun playTrack(track: Track) {
        Timber.d("Play track: ${track.title}")
        // TODO: Integrate with PlaybackService
    }

    fun playAll() {
        val state = _uiState.value
        if (state is TrackListUiState.Success) {
            Timber.d("Play all ${state.tracks.size} tracks")
            // TODO: Add all tracks to queue and start playback
        }
    }
}

sealed class TrackListUiState {
    data object Loading : TrackListUiState()
    data class Success(val tracks: List<Track>) : TrackListUiState()
    data object Empty : TrackListUiState()
    data class Error(val message: String) : TrackListUiState()
}
