package com.plexglassplayer.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.LibraryRepository
import com.plexglassplayer.data.repositories.PlaybackRepository
import com.plexglassplayer.feature.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
    val playbackManager: PlaybackManager
) : ViewModel() {

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTracks()
    }

    fun updateUserName(newName: String) {
        _userName.value = newName
    }

    fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Fetch Recent and All Tracks in parallel
            val recentDeferred = async { libraryRepository.getRecentTracks(limit = 10) }
            val allDeferred = async { libraryRepository.getAllTracks(offset = 0, limit = 200) }

            val recentResult = recentDeferred.await()
            val allResult = allDeferred.await()

            if (allResult is Result.Success && recentResult is Result.Success) {
                _uiState.value = HomeUiState.Success(
                    recentTracks = recentResult.data,
                    allTracks = allResult.data
                )
            } else if (allResult is Result.Error) {
                _uiState.value = HomeUiState.Error(allResult.exception.message ?: "Error")
            } else {
                _uiState.value = HomeUiState.Error("Unknown error")
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is HomeUiState.Success) {
                    // Play context depends on where the user clicked (Recent or All)
                    // For simplicity, we just queue this specific track, or queue surrounding tracks
                    // Here we just play the single track or find it in the 'all' list
                    val queueItems = playbackRepository.convertTracksToQueue(listOf(track))
                    playbackManager.playTracks(queueItems, 0)
                    Timber.d("Started playback: ${track.title}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start playback")
            }
        }
    }
}

// Updated State to hold both lists
sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val recentTracks: List<Track>,
        val allTracks: List<Track>
    ) : HomeUiState()
    data object Empty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}