package com.plexglassplayer.domain.usecase

import com.plexglassplayer.core.model.Artist
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.LibraryRepository
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(offset: Int = 0, limit: Int = 50): Result<List<Artist>> {
        return libraryRepository.getArtists(offset, limit)
    }
}
