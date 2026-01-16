package com.plexglassplayer.feature.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.plexglassplayer.core.model.QueueItem
import com.plexglassplayer.core.model.RepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentTrack = MutableStateFlow<QueueItem?>(null)
    val currentTrack: StateFlow<QueueItem?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _queue = MutableStateFlow<List<QueueItem>>(emptyList())
    val queue: StateFlow<List<QueueItem>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
                Timber.d("MediaController connected")
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duration.value = mediaController?.duration ?: 0L
                    }
                    Player.STATE_ENDED -> {
                        playNext()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrack()
            }
        })
    }

    private fun updateCurrentTrack() {
        val currentMediaIndex = mediaController?.currentMediaItemIndex ?: 0
        if (currentMediaIndex >= 0 && currentMediaIndex < _queue.value.size) {
            _currentTrack.value = _queue.value[currentMediaIndex]
            _currentIndex.value = currentMediaIndex
        }
    }

    fun playTrack(track: QueueItem) {
        playTracks(listOf(track), 0)
    }

    fun playTracks(tracks: List<QueueItem>, startIndex: Int = 0) {
        Timber.d("Playing ${tracks.size} tracks, starting at $startIndex")
        _queue.value = tracks
        _currentIndex.value = startIndex

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.uri)
                .setMediaId(track.trackId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.artworkUrl?.let { android.net.Uri.parse(it) })
                        .build()
                )
                .build()
        }

        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
        }

        updateCurrentTrack()
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun playPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun playNext() {
        mediaController?.seekToNext()
    }

    fun playPrevious() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun setRepeatMode(repeatMode: RepeatMode) {
        _repeatMode.value = repeatMode
        val playerRepeatMode = when (repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        mediaController?.repeatMode = playerRepeatMode
    }

    fun toggleRepeatMode() {
        val newMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatMode(newMode)
    }

    fun setShuffle(enabled: Boolean) {
        _shuffleEnabled.value = enabled
        mediaController?.shuffleModeEnabled = enabled
    }

    fun toggleShuffle() {
        setShuffle(!_shuffleEnabled.value)
    }

    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    /**
     * Play a specific track from the queue by index
     */
    fun playFromQueue(index: Int) {
        if (index >= 0 && index < _queue.value.size) {
            mediaController?.seekToDefaultPosition(index)
            play()
        }
    }

    /**
     * Remove a track from the queue
     */
    fun removeFromQueue(index: Int) {
        if (index >= 0 && index < _queue.value.size && index != _currentIndex.value) {
            val newQueue = _queue.value.toMutableList()
            newQueue.removeAt(index)
            _queue.value = newQueue

            // Rebuild media items
            val mediaItems = newQueue.map { track ->
                MediaItem.Builder()
                    .setUri(track.uri)
                    .setMediaId(track.trackId)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .setArtworkUri(track.artworkUrl?.let { android.net.Uri.parse(it) })
                            .build()
                    )
                    .build()
            }

            val currentPos = getCurrentPosition()
            val currentIdx = _currentIndex.value

            mediaController?.apply {
                setMediaItems(mediaItems, if (index < currentIdx) currentIdx - 1 else currentIdx, currentPos)
            }

            if (index < currentIdx) {
                _currentIndex.value = currentIdx - 1
            }
        }
    }

    /**
     * Clear the entire queue
     */
    fun clearQueue() {
        mediaController?.stop()
        mediaController?.clearMediaItems()
        _queue.value = emptyList()
        _currentTrack.value = null
        _currentIndex.value = 0
    }

    /**
     * Add track to end of queue
     */
    fun addToQueue(track: QueueItem) {
        val newQueue = _queue.value.toMutableList()
        newQueue.add(track)
        _queue.value = newQueue

        val mediaItem = MediaItem.Builder()
            .setUri(track.uri)
            .setMediaId(track.trackId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.artworkUrl?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()

        mediaController?.addMediaItem(mediaItem)
    }

    fun release() {
        mediaController?.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
