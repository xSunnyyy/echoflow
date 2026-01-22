package com.plexglassplayer.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.DownloadManager
import com.plexglassplayer.data.repositories.PlaybackRepository
import com.plexglassplayer.domain.usecase.GetTracksUseCase
import com.plexglassplayer.feature.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// Defined at top to resolve unresolved reference errors
sealed class TrackListUiState {
    data object Loading : TrackListUiState()
    data class Success(val tracks: List<Track>) : TrackListUiState()
    data object Empty : TrackListUiState()
    data class Error(val message: String) : TrackListUiState()
}

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val playbackRepository: PlaybackRepository,
    private val playbackManager: PlaybackManager,
    private val downloadManager: DownloadManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: String? = savedStateHandle["albumId"]

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
                    val tracks = if (albumId == null) {
                        result.data.sortedBy { it.title.lowercase() }
                    } else {
                        result.data
                    }

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
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is TrackListUiState.Success) {
                    // This creates a queue from the current visible list (could be one artist)
                    val queueItems = playbackRepository.convertTracksToQueue(state.tracks)
                    val startIndex = state.tracks.indexOfFirst { it.id == track.id }
                    playbackManager.playTracks(queueItems, if (startIndex != -1) startIndex else 0)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start playback")
            }
        }
    }

    fun playAll() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is TrackListUiState.Success) {
                    val queueItems = playbackRepository.convertTracksToQueue(state.tracks)
                    playbackManager.playTracks(queueItems, 0)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to play all tracks")
            }
        }
    }

    // --- FIX: TRUE GLOBAL SHUFFLE ---
    fun shuffleAll() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is TrackListUiState.Success) {
                    // 1. Manually shuffle the entire list provided by the UseCase
                    // If albumId is null, this is your entire library
                    val tracks = state.tracks.shuffled()

                    // 2. Convert the full, randomized library into QueueItems
                    val queueItems = playbackRepository.convertTracksToQueue(tracks)

                    // 3. Play from the start of this new global randomized queue
                    playbackManager.playTracks(queueItems, 0)

                    // 4. Ensure internal shuffle mode is off to prevent ExoPlayer
                    // from recalculating a new (possibly biased) shuffle path
                    playbackManager.setShuffleEnabled(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to shuffle tracks")
            }
        }
    }
}