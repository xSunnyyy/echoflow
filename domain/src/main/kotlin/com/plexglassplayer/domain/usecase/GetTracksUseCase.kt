package com.plexglassplayer.domain.usecase

import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.LibraryRepository
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    // FIX: Changed default limit from 50 to 10000 to ensure ALL songs load
    suspend operator fun invoke(albumId: String?, offset: Int = 0, limit: Int = 10000): Result<List<Track>> {
        return if (albumId == null) {
            // "All Music" mode
            libraryRepository.getAllTracks(offset, limit)
        } else {
            // "Album" mode
            libraryRepository.getTracks(albumId, offset, limit)
        }
    }
}