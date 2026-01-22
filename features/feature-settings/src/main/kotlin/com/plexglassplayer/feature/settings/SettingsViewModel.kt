package com.plexglassplayer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.data.auth.AuthRepository
import com.plexglassplayer.data.repositories.ServerPreferences
import com.plexglassplayer.data.repositories.UserPreferences // Import this
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
    private val serverPreferences: ServerPreferences,
    private val userPreferences: UserPreferences // Inject UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // --- NEW: Expose username to UI ---
    val currentName: StateFlow<String> = userPreferences.userName

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val serverUrl = serverPreferences.getActiveServerUrl()
                val serverId = serverPreferences.activeServerIdFlow.first()

                _uiState.value = SettingsUiState.Success(
                    serverName = "Plex Server",
                    serverUrl = serverUrl ?: "",
                    serverId = serverId ?: ""
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load settings")
                _uiState.value = SettingsUiState.Error("Failed to load settings")
            }
        }
    }

    // --- NEW: Save username ---
    fun updateName(name: String) {
        userPreferences.saveUserName(name)
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
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

sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Success(
        val serverName: String,
        val serverUrl: String,
        val serverId: String
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}