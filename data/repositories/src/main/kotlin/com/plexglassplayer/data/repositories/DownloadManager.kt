package com.plexglassplayer.data.repositories

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.plexglassplayer.core.db.dao.DownloadDao
import com.plexglassplayer.core.db.entity.DownloadDbEntity
import com.plexglassplayer.core.db.entity.toModel
import com.plexglassplayer.core.model.DownloadEntity
import com.plexglassplayer.core.model.DownloadStatus
import com.plexglassplayer.core.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val serverPreferences: ServerPreferences,
    private val workManager: WorkManager
) {

    /**
     * Observe all downloads
     */
    fun getAllDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getAllDownloads().map { list ->
            list.map { it.toModel() }
        }
    }

    /**
     * Observe downloads by status
     */
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>> {
        return downloadDao.getDownloadsByStatus(status.name).map { list ->
            list.map { it.toModel() }
        }
    }

    /**
     * Check if a track is downloaded
     */
    suspend fun isTrackDownloaded(trackId: String): Boolean {
        val serverId = serverPreferences.activeServerIdFlow.first() ?: return false
        val download = downloadDao.findCompletedDownload(serverId, trackId)
        return download != null && download.filePath?.let { java.io.File(it).exists() } == true
    }

    /**
     * Download a single track
     */
    suspend fun downloadTrack(track: Track) {
        val serverId = serverPreferences.activeServerIdFlow.first() ?: run {
            Timber.w("Cannot download: no active server")
            return
        }

        val downloadId = "${serverId}_${track.id}"

        // Check if already downloading or completed
        val existing = downloadDao.getDownloadById(downloadId)
        if (existing != null && (existing.status == "DOWNLOADING" || existing.status == "COMPLETED")) {
            Timber.d("Track already downloading or downloaded: ${track.title}")
            return
        }

        // Create download entity
        val now = System.currentTimeMillis()
        val downloadEntity = DownloadDbEntity(
            id = downloadId,
            serverId = serverId,
            trackId = track.id,
            title = track.title,
            artist = track.artistName,
            album = track.albumTitle,
            artworkUrl = track.artUrl,
            status = "QUEUED",
            progressPct = 0,
            bytesDownloaded = 0,
            bytesTotal = null,
            filePath = null,
            errorMessage = null,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )

        // Insert into database
        downloadDao.insertDownload(downloadEntity)

        // Create work request
        val streamUrl = "${serverPreferences.getActiveServerUrl()}${track.streamKey}"
        val fileName = sanitizeFileName("${track.artistName} - ${track.title}.${getExtension(track.streamKey)}")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_DOWNLOAD_ID to downloadId,
                    DownloadWorker.KEY_STREAM_URL to streamUrl,
                    DownloadWorker.KEY_FILE_NAME to fileName
                )
            )
            .addTag(TAG_DOWNLOAD)
            .addTag(downloadId)
            .build()

        // Enqueue work
        workManager.enqueueUniqueWork(
            downloadId,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        Timber.d("Enqueued download: ${track.title}")
    }

    /**
     * Download multiple tracks (album or playlist)
     */
    suspend fun downloadTracks(tracks: List<Track>) {
        tracks.forEach { track ->
            downloadTrack(track)
        }
    }

    /**
     * Cancel a download
     */
    suspend fun cancelDownload(downloadId: String) {
        workManager.cancelAllWorkByTag(downloadId)
        downloadDao.deleteDownloadById(downloadId)
        Timber.d("Cancelled download: $downloadId")
    }

    /**
     * Retry a failed download
     */
    suspend fun retryDownload(downloadId: String) {
        val download = downloadDao.getDownloadById(downloadId)
        if (download != null && download.status == "FAILED") {
            // Update status to queued
            downloadDao.updateProgress(
                id = downloadId,
                status = "QUEUED",
                progress = 0,
                bytesDownloaded = 0,
                updatedAt = System.currentTimeMillis()
            )

            // Re-enqueue work
            val streamUrl = "${serverPreferences.getActiveServerUrl()}/library/parts/${download.trackId}"
            val fileName = sanitizeFileName("${download.artist} - ${download.title}.mp3")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        DownloadWorker.KEY_DOWNLOAD_ID to downloadId,
                        DownloadWorker.KEY_STREAM_URL to streamUrl,
                        DownloadWorker.KEY_FILE_NAME to fileName
                    )
                )
                .addTag(TAG_DOWNLOAD)
                .addTag(downloadId)
                .build()

            workManager.enqueueUniqueWork(
                downloadId,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Timber.d("Retrying download: $downloadId")
        }
    }

    /**
     * Delete a completed download
     */
    suspend fun deleteDownload(downloadId: String) {
        val download = downloadDao.getDownloadById(downloadId)
        if (download != null) {
            // Delete file if exists
            download.filePath?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) {
                    file.delete()
                }
            }

            // Cancel work if running
            workManager.cancelAllWorkByTag(downloadId)

            // Delete from database
            downloadDao.deleteDownloadById(downloadId)

            Timber.d("Deleted download: $downloadId")
        }
    }

    /**
     * Get work info for a download
     */
    fun getDownloadWorkInfo(downloadId: String): Flow<WorkInfo?> {
        return workManager.getWorkInfosByTagFlow(downloadId).map { workInfos ->
            workInfos.firstOrNull()
        }
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
    }

    private fun getExtension(streamKey: String): String {
        // Try to extract extension from stream key
        val parts = streamKey.split(".")
        return if (parts.size > 1) {
            parts.last()
        } else {
            "mp3" // Default to mp3
        }
    }

    companion object {
        private const val TAG_DOWNLOAD = "download"
    }
}
