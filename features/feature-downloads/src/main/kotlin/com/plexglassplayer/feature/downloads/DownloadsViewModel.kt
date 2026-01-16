package com.plexglassplayer.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexglassplayer.core.model.DownloadEntity
import com.plexglassplayer.core.model.DownloadStatus
import com.plexglassplayer.data.repositories.DownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadManager: DownloadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DownloadsUiState>(DownloadsUiState.Loading)
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            try {
                downloadManager.getAllDownloads().collect { downloads ->
                    if (downloads.isEmpty()) {
                        _uiState.value = DownloadsUiState.Empty
                    } else {
                        _uiState.value = DownloadsUiState.Success(
                            activeDownloads = downloads.filter {
                                it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED
                            },
                            completedDownloads = downloads.filter { it.status == DownloadStatus.COMPLETED },
                            failedDownloads = downloads.filter { it.status == DownloadStatus.FAILED }
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load downloads")
                _uiState.value = DownloadsUiState.Error("Failed to load downloads")
            }
        }
    }

    fun cancelDownload(downloadId: String) {
        viewModelScope.launch {
            try {
                downloadManager.cancelDownload(downloadId)
                Timber.d("Cancelled download: $downloadId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to cancel download")
            }
        }
    }

    fun retryDownload(downloadId: String) {
        viewModelScope.launch {
            try {
                downloadManager.retryDownload(downloadId)
                Timber.d("Retrying download: $downloadId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to retry download")
            }
        }
    }

    fun deleteDownload(downloadId: String) {
        viewModelScope.launch {
            try {
                downloadManager.deleteDownload(downloadId)
                Timber.d("Deleted download: $downloadId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete download")
            }
        }
    }
}

sealed class DownloadsUiState {
    data object Loading : DownloadsUiState()
    data object Empty : DownloadsUiState()
    data class Success(
        val activeDownloads: List<DownloadEntity>,
        val completedDownloads: List<DownloadEntity>,
        val failedDownloads: List<DownloadEntity>
    ) : DownloadsUiState()
    data class Error(val message: String) : DownloadsUiState()
}
