package com.plexglassplayer.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Album
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.domain.usecase.GetAlbumsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AlbumListViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: String? = savedStateHandle["artistId"]

    private val _uiState = MutableStateFlow<AlbumListUiState>(AlbumListUiState.Loading)
    val uiState: StateFlow<AlbumListUiState> = _uiState.asStateFlow()

    private val albums = mutableListOf<Album>()
    private var currentOffset = 0
    private val pageSize = 50
    private var hasMorePages = true

    init {
        loadAlbums()
    }

    fun loadAlbums(isRefresh: Boolean = false) {
        if (isRefresh) {
            albums.clear()
            currentOffset = 0
            hasMorePages = true
        }

        if (!hasMorePages) return

        viewModelScope.launch {
            if (albums.isEmpty()) {
                _uiState.value = AlbumListUiState.Loading
            }

            when (val result = getAlbumsUseCase(artistId = artistId, offset = currentOffset, limit = pageSize)) {
                is Result.Success -> {
                    val newAlbums = result.data
                    albums.addAll(newAlbums)
                    currentOffset += newAlbums.size
                    hasMorePages = newAlbums.size >= pageSize

                    if (albums.isEmpty()) {
                        _uiState.value = AlbumListUiState.Empty
                    } else {
                        _uiState.value = AlbumListUiState.Success(
                            albums = albums.toList(),
                            hasMore = hasMorePages
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to load albums")
                    if (albums.isEmpty()) {
                        _uiState.value = AlbumListUiState.Error(
                            result.exception.message ?: "Failed to load albums"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun loadMore() {
        if (hasMorePages && _uiState.value is AlbumListUiState.Success) {
            loadAlbums()
        }
    }
}

sealed class AlbumListUiState {
    data object Loading : AlbumListUiState()
    data class Success(val albums: List<Album>, val hasMore: Boolean) : AlbumListUiState()
    data object Empty : AlbumListUiState()
    data class Error(val message: String) : AlbumListUiState()
}
