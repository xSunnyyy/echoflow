package com.plexglassplayer.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.auth.AuthRepository
import com.plexglassplayer.data.auth.PinAuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun startAuth() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            when (val result = authRepository.startPinAuth()) {
                is Result.Success -> {
                    val pinData = result.data
                    _uiState.value = AuthUiState.PinGenerated(pinData)

                    // Start polling for auth completion
                    pollForAuth(pinData.pinId)
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Failed to start auth")
                    _uiState.value = AuthUiState.Error(
                        result.exception.message ?: "Failed to start authentication"
                    )
                }
                else -> {}
            }
        }
    }

    private fun pollForAuth(pinId: String) {
        viewModelScope.launch {
            when (val result = authRepository.pollForAuth(pinId)) {
                is Result.Success -> {
                    Timber.d("Auth completed successfully")
                    _uiState.value = AuthUiState.Success
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Auth failed")
                    _uiState.value = AuthUiState.Error(
                        result.exception.message ?: "Authentication failed"
                    )
                }
                else -> {}
            }
        }
    }

    fun retry() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class PinGenerated(val pinData: PinAuthData) : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
