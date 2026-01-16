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

@HiltViewModel
class TrackListViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val playbackRepository: PlaybackRepository,
    private val playbackManager: PlaybackManager,
    private val downloadManager: DownloadManager,
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
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is TrackListUiState.Success) {
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

    fun playAll() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is TrackListUiState.Success) {
                    val queueItems = playbackRepository.convertTracksToQueue(state.tracks)
                    playbackManager.playTracks(queueItems, 0)
                    Timber.d("Playing all ${state.tracks.size} tracks")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to play all tracks")
            }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            try {
                downloadManager.downloadTrack(track)
                Timber.d("Started download: ${track.title}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to start download")
            }
        }
    }

    fun downloadAll() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is TrackListUiState.Success) {
                    downloadManager.downloadTracks(state.tracks)
                    Timber.d("Started downloading ${state.tracks.size} tracks")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to download all tracks")
            }
        }
    }
}

sealed class TrackListUiState {
    data object Loading : TrackListUiState()
    data class Success(val tracks: List<Track>) : TrackListUiState()
    data object Empty : TrackListUiState()
    data class Error(val message: String) : TrackListUiState()
}
