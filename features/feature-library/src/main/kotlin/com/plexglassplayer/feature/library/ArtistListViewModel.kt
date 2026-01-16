package com.plexglassplayer.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.Artist
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.domain.usecase.GetArtistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArtistListViewModel @Inject constructor(
    private val getArtistsUseCase: GetArtistsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArtistListUiState>(ArtistListUiState.Loading)
    val uiState: StateFlow<ArtistListUiState> = _uiState.asStateFlow()

    private val artists = mutableListOf<Artist>()
    private var currentOffset = 0
    private val pageSize = 50
    private var hasMorePages = true

    init {
        loadArtists()
    }

    fun loadArtists(isRefresh: Boolean = false) {
        if (isRefresh) {
            artists.clear()
            currentOffset = 0
            hasMorePages = true
        }

        if (!hasMorePages) return

        viewModelScope.launch {
            if (artists.isEmpty()) {
                _uiState.value = ArtistListUiState.Loading
            }

            when (val result = getArtistsUseCase(offset = currentOffset, limit = pageSize)) {
                is Result.Success -> {
                    val newArtists = result.data
                    artists.addAll(newArtists)
                    currentOffset += newArtists.size
                    hasMorePages = newArtists.size >= pageSize

                    if (artists.isEmpty()) {
                        _uiState.value = ArtistListUiState.Empty
                    } else {
                        _uiState.value = ArtistListUiState.Success(
                            artists = artists.toList(),
                            hasMore = hasMorePages
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to load artists")
                    if (artists.isEmpty()) {
                        _uiState.value = ArtistListUiState.Error(
                            result.exception.message ?: "Failed to load artists"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun loadMore() {
        if (hasMorePages && _uiState.value is ArtistListUiState.Success) {
            loadArtists()
        }
    }
}

sealed class ArtistListUiState {
    data object Loading : ArtistListUiState()
    data class Success(val artists: List<Artist>, val hasMore: Boolean) : ArtistListUiState()
    data object Empty : ArtistListUiState()
    data class Error(val message: String) : ArtistListUiState()
}
