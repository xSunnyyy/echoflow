package com.plexglassplayer.data.repositories

import android.net.Uri
import com.plexglassplayer.core.db.dao.DownloadDao
import com.plexglassplayer.core.model.MediaSourceType
import com.plexglassplayer.core.model.QueueItem
import com.plexglassplayer.core.model.Track
import com.plexglassplayer.data.auth.SessionStore
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackRepository @Inject constructor(
    private val sessionStore: SessionStore,
    private val serverPreferences: ServerPreferences,
    private val downloadDao: DownloadDao
) {

    /**
     * Resolve playback URI for a track
     * Prefers local file if downloaded, otherwise builds streaming URL
     */
    suspend fun resolvePlaybackUri(track: Track): Uri {
        val trackId = requireTrackId(track)

        val serverUrl = serverPreferences.getActiveServerUrl() ?: ""
        val download = downloadDao.findCompletedDownload(serverUrl, trackId)

        return if (download?.filePath != null) {
            val file = File(download.filePath)
            if (file.exists()) {
                Timber.d("Playing local file: ${download.filePath}")
                Uri.fromFile(file)
            } else {
                Timber.w("Downloaded file not found, streaming instead")
                buildStreamingUri(track)
            }
        } else {
            buildStreamingUri(track)
        }
    }

    /**
     * Build streaming URL for a track
     */
    private suspend fun buildStreamingUri(track: Track): Uri {
        val baseUrl = serverPreferences.getActiveServerUrl()
            ?: throw IllegalStateException("No active server")
        val token = sessionStore.getAccessToken()
            ?: throw IllegalStateException("No auth token")

        val streamKey = requireStreamKey(track)

        return Uri.parse("$baseUrl$streamKey")
            .buildUpon()
            .appendQueryParameter("X-Plex-Token", token)
            .build()
    }

    /**
     * Convert track to queue item
     */
    suspend fun trackToQueueItem(track: Track): QueueItem {
        val trackId = requireTrackId(track)

        val uri = resolvePlaybackUri(track)
        val isLocal = uri.scheme == "file"

        return QueueItem(
            trackId = trackId,
            title = track.title ?: "Unknown Title",
            artist = track.artistName ?: "Unknown Artist",
            album = track.albumTitle ?: "",
            artworkUrl = track.artUrl ?: "",
            source = if (isLocal) MediaSourceType.LOCAL else MediaSourceType.STREAM,
            uri = uri.toString()
        )
    }

    /**
     * Convert multiple tracks to queue items
     * Matches TrackListViewModel's expectation
     */
    suspend fun convertTracksToQueue(tracks: List<Track>): List<QueueItem> {
        return tracks.map { trackToQueueItem(it) }
    }

    private fun requireTrackId(track: Track): String {
        val id = track.id
        if (id.isNullOrBlank()) {
            throw IllegalArgumentException("Track.id is null/blank; cannot resolve playback or queue item.")
        }
        return id
    }

    private fun requireStreamKey(track: Track): String {
        val key = track.streamKey
        if (key.isNullOrBlank()) {
            throw IllegalArgumentException("Track.streamKey is null/blank; cannot build streaming URL.")
        }
        return key
    }
}
