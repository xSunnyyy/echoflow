package com.plexglassplayer.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.LibraryRepository
import com.plexglassplayer.data.repositories.PlaybackRepository
import com.plexglassplayer.feature.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTracks()
    }

    fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            when (val result = libraryRepository.getAllTracks(offset = 0, limit = 200)) {
                is Result.Success -> {
                    val tracks = result.data
                    if (tracks.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        _uiState.value = HomeUiState.Success(tracks)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to load tracks")
                    _uiState.value = HomeUiState.Error(
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
                if (state is HomeUiState.Success) {
                    val queueItems = playbackRepository.convertTracksToQueue(state.tracks)
                    val startIndex = state.tracks.indexOfFirst { it.id == track.id }
                    playbackManager.playTracks(queueItems, if (startIndex != -1) startIndex else 0)
                    Timber.d("Started playback: ${track.title}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start playback")
            }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val tracks: List<Track>) : HomeUiState()
    data object Empty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
