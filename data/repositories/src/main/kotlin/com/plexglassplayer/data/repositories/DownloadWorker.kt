package com.plexglassplayer.data.repositories

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.plexglassplayer.core.db.dao.DownloadDao
import com.plexglassplayer.data.auth.SessionStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val sessionStore: SessionStore,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadId = inputData.getString(KEY_DOWNLOAD_ID) ?: return@withContext Result.failure()
        val streamUrl = inputData.getString(KEY_STREAM_URL) ?: return@withContext Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return@withContext Result.failure()

        try {
            // Update status to downloading
            downloadDao.updateProgress(
                id = downloadId,
                status = "DOWNLOADING",
                progress = 0,
                bytesDownloaded = 0,
                updatedAt = System.currentTimeMillis()
            )

            // Get auth token
            val token = sessionStore.getAccessToken()

            // Build request with auth
            val request = Request.Builder()
                .url("$streamUrl?X-Plex-Token=$token")
                .build()

            // Execute download
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Download failed: ${response.code}")
            }

            val body = response.body ?: throw Exception("Response body is null")
            val contentLength = body.contentLength()

            // Create download directory
            val downloadDir = File(applicationContext.filesDir, "downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val outputFile = File(downloadDir, fileName)
            var totalBytesRead = 0L

            body.byteStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead = input.read(buffer)

                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        // Update progress
                        val progress = if (contentLength > 0) {
                            ((totalBytesRead * 100) / contentLength).toInt()
                        } else {
                            0
                        }

                        downloadDao.updateProgress(
                            id = downloadId,
                            status = "DOWNLOADING",
                            progress = progress,
                            bytesDownloaded = totalBytesRead,
                            updatedAt = System.currentTimeMillis()
                        )

                        // Report progress
                        setProgress(workDataOf(
                            KEY_PROGRESS to progress,
                            KEY_BYTES_DOWNLOADED to totalBytesRead
                        ))

                        bytesRead = input.read(buffer)
                    }
                }
            }

            // Mark as completed
            downloadDao.markCompleted(
                id = downloadId,
                filePath = outputFile.absolutePath,
                updatedAt = System.currentTimeMillis()
            )

            Timber.d("Download completed: $fileName")
            Result.success(workDataOf(KEY_FILE_PATH to outputFile.absolutePath))

        } catch (e: Exception) {
            Timber.e(e, "Download failed: $downloadId")

            downloadDao.markFailed(
                id = downloadId,
                errorMessage = e.message ?: "Unknown error",
                updatedAt = System.currentTimeMillis()
            )

            Result.failure(workDataOf(KEY_ERROR_MESSAGE to (e.message ?: "Download failed")))
        }
    }

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_STREAM_URL = "stream_url"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_PROGRESS = "progress"
        const val KEY_BYTES_DOWNLOADED = "bytes_downloaded"
        const val KEY_FILE_PATH = "file_path"
        const val KEY_ERROR_MESSAGE = "error_message"
    }
}
