package com.plexglassplayer.feature.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Playlist
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.model.QueueItem
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            // Using 'if' here is fine because we only care about Success
            val result = libraryRepository.getPlaylists()
            if (result is Result.Success) {
                _playlists.value = result.data
            }
        }
    }

    fun addToPlaylist(playlist: Playlist, queueItem: QueueItem) {
        viewModelScope.launch {
            Timber.d("Adding track to playlist: trackId=${queueItem.trackId}, playlistId=${playlist.id}")

            val track = Track(
                id = queueItem.trackId,
                title = queueItem.title,
                artistName = queueItem.artist,
                albumTitle = "",
                durationMs = 0,
                trackNumber = 0,
                artUrl = queueItem.artworkUrl,
                streamKey = ""
            )

            val result = libraryRepository.addToPlaylist(playlist.id, track)

            // FIX: Added 'else' branch to handle other states (like Loading)
            when (result) {
                is Result.Success -> {
                    Timber.d("Successfully added track to playlist")
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to add track to playlist")
                }
                else -> {
                    // Handle Loading or other states if necessary
                }
            }
        }
    }

    // --- Create Playlist ---
    fun createPlaylist(name: String, queueItem: QueueItem) {
        viewModelScope.launch {
            val track = Track(
                id = queueItem.trackId,
                title = queueItem.title,
                artistName = queueItem.artist,
                albumTitle = "",
                durationMs = 0,
                trackNumber = 0,
                artUrl = queueItem.artworkUrl,
                streamKey = ""
            )
            libraryRepository.createPlaylist(name, track)
            loadPlaylists() // Refresh the playlist list
        }
    }
}