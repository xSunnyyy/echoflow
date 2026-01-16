package com.plexglassplayer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.data.auth.AuthRepository
import com.plexglassplayer.data.repositories.ServerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val serverPreferences: ServerPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val serverUrl = serverPreferences.getActiveServerUrl()
                // Use the first value from the existing flow for the ID
                val serverId = serverPreferences.activeServerIdFlow.first() 
                
                // Note: Server Name is currently not stored in ServerPreferences
                _uiState.value = SettingsUiState.Success(
                    serverName = "Plex Server", // Placeholder or update ServerPreferences
                    serverUrl = serverUrl ?: "",
                    serverId = serverId ?: ""
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load settings")
                _uiState.value = SettingsUiState.Error("Failed to load settings")
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Changed from clearSession() to signOut() to match AuthRepository.kt
                authRepository.signOut() 
                serverPreferences.clearActiveServer()
                Timber.d("Logged out successfully")
                onLogoutComplete()
            } catch (e: Exception) {
                Timber.e(e, "Failed to logout")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                // TODO: Implement cache clearing
                Timber.d("Cache cleared")
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear cache")
            }
        }
    }
}
