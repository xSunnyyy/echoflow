package com.plexglassplayer.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Playlist
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
import kotlinx.coroutines.flow.update
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadTracks()
    }

    fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            fetchData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchData()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchData() {
        val recentDeferred = viewModelScope.async { libraryRepository.getRecentTracks(limit = 10) }
        val allDeferred = viewModelScope.async { libraryRepository.getAllTracks(offset = 0, limit = 500) }
        val playlistsDeferred = viewModelScope.async { libraryRepository.getPlaylists() }

        val recentResult = recentDeferred.await()
        val allResult = allDeferred.await()
        val playlistsResult = playlistsDeferred.await()

        if (allResult is Result.Success && recentResult is Result.Success && playlistsResult is Result.Success) {
            _uiState.value = HomeUiState.Success(
                recentTracks = recentResult.data,
                allTracks = allResult.data,
                playlists = playlistsResult.data
            )
        } else if (allResult is Result.Error) {
            _uiState.value = HomeUiState.Error(allResult.exception.message ?: "Error")
        } else {
            _uiState.value = HomeUiState.Error("Unknown error")
        }
    }

    // ... (Keep existing methods: playTrack, openPlaylist, etc. They are fine)

    fun playTrack(track: Track) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                if (state is HomeUiState.Success) {
                    val allTracks = state.allTracks
                    val indexInAll = allTracks.indexOfFirst { it.id == track.id }
                    val (tracksToPlay, startIndex) = if (indexInAll != -1) {
                        allTracks to indexInAll
                    } else {
                        (listOf(track) + allTracks) to 0
                    }
                    val queueItems = playbackRepository.convertTracksToQueue(tracksToPlay)
                    playbackManager.playTracks(queueItems, startIndex)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start playback")
            }
        }
    }

    fun openPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val result = libraryRepository.getPlaylistItems(playlist.id)
            if (result is Result.Success) {
                _uiState.update { currentState ->
                    if (currentState is HomeUiState.Success) {
                        currentState.copy(
                            selectedPlaylist = playlist,
                            selectedPlaylistTracks = result.data
                        )
                    } else currentState
                }
            }
        }
    }

    fun closePlaylistSheet() {
        _uiState.update { currentState ->
            if (currentState is HomeUiState.Success) {
                currentState.copy(selectedPlaylist = null, selectedPlaylistTracks = emptyList())
            } else currentState
        }
    }

    fun playFromPlaylist(index: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HomeUiState.Success && currentState.selectedPlaylistTracks.isNotEmpty()) {
                val queueItems = playbackRepository.convertTracksToQueue(currentState.selectedPlaylistTracks)
                playbackManager.playTracks(queueItems, index)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val allTracks = (_uiState.value as? HomeUiState.Success)?.allTracks
            val firstTrack = allTracks?.firstOrNull()
            if (firstTrack != null) {
                libraryRepository.createPlaylist(name, firstTrack)
                refresh()
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                libraryRepository.deletePlaylist(playlist.id)
                _uiState.update { currentState ->
                    if (currentState is HomeUiState.Success) {
                        currentState.copy(playlists = currentState.playlists.filter { it.id != playlist.id })
                    } else currentState
                }
                refresh()
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete playlist")
            }
        }
    }

    fun removeTrackFromPlaylist(track: Track) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HomeUiState.Success && currentState.selectedPlaylist != null) {
                try {
                    val playlistItemId = track.playlistItemId ?: track.id
                    libraryRepository.removeTrackFromPlaylist(currentState.selectedPlaylist.id, playlistItemId)
                    val updatedTracks = currentState.selectedPlaylistTracks.filter { it.id != track.id }
                    _uiState.update {
                        (it as HomeUiState.Success).copy(selectedPlaylistTracks = updatedTracks)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to remove track from playlist")
                }
            }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val recentTracks: List<Track>,
        val allTracks: List<Track>,
        val playlists: List<Playlist>,
        val selectedPlaylist: Playlist? = null,
        val selectedPlaylistTracks: List<Track> = emptyList()
    ) : HomeUiState()
    data object Empty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}