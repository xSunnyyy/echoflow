package com.plexglassplayer.domain.usecase

import com.plexglassplayer.core.model.Track
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.LibraryRepository
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(query: String, offset: Int = 0, limit: Int = 50): Result<List<Track>> {
        return libraryRepository.search(query, offset, limit)
    }
}
