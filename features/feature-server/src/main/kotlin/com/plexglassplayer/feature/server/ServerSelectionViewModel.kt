package com.plexglassplayer.feature.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.PlexServer
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.ServerPreferences
import com.plexglassplayer.data.repositories.ServerRepository
import com.plexglassplayer.domain.usecase.GetServersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServerSelectionViewModel @Inject constructor(
    private val getServersUseCase: GetServersUseCase,
    private val serverRepository: ServerRepository,
    private val serverPreferences: ServerPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServerSelectionUiState>(ServerSelectionUiState.Loading)
    val uiState: StateFlow<ServerSelectionUiState> = _uiState.asStateFlow()

    init {
        loadServers()
    }

    fun loadServers() {
        viewModelScope.launch {
            _uiState.value = ServerSelectionUiState.Loading

            when (val result = getServersUseCase()) {
                is Result.Success -> {
                    val servers = result.data
                    if (servers.isEmpty()) {
                        _uiState.value = ServerSelectionUiState.Empty
                    } else {
                        _uiState.value = ServerSelectionUiState.Success(servers)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to load servers")
                    _uiState.value = ServerSelectionUiState.Error(
                        result.exception.message ?: "Failed to load servers"
                    )
                }
                else -> {}
            }
        }
    }

    fun selectServer(server: PlexServer) {
        viewModelScope.launch {
            _uiState.value = ServerSelectionUiState.Selecting(server)

            // Get the best connection URL
            val connectionUrl = server.preferredConnectionUrl
                ?: server.connections.firstOrNull()?.uri

            if (connectionUrl == null) {
                _uiState.value = ServerSelectionUiState.Error("Server has no available connections")
                return@launch
            }

            // Fetch library sections to find music section
            when (val result = serverRepository.getLibrarySections(connectionUrl)) {
                is Result.Success -> {
                    val musicSections = result.data
                    if (musicSections.isEmpty()) {
                        _uiState.value = ServerSelectionUiState.Error(
                            "No music library found on this server"
                        )
                    } else {
                        // Use the first music section
                        val musicSectionKey = musicSections.first()

                        // Save server configuration
                        serverPreferences.setActiveServer(
                            serverId = server.id,
                            serverUrl = connectionUrl,
                            musicSectionKey = musicSectionKey
                        )

                        Timber.d("Selected server: ${server.name} with music section: $musicSectionKey")
                        _uiState.value = ServerSelectionUiState.ServerSelected
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to get library sections")
                    _uiState.value = ServerSelectionUiState.Error(
                        "Failed to connect to server: ${result.exception.message}"
                    )
                }
                else -> {}
            }
        }
    }
}

sealed class ServerSelectionUiState {
    data object Loading : ServerSelectionUiState()
    data class Success(val servers: List<PlexServer>) : ServerSelectionUiState()
    data class Selecting(val server: PlexServer) : ServerSelectionUiState()
    data object ServerSelected : ServerSelectionUiState()
    data object Empty : ServerSelectionUiState()
    data class Error(val message: String) : ServerSelectionUiState()
}
