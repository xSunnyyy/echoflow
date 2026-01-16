package com.plexglassplayer.domain.usecase

import com.plexglassplayer.core.model.Album
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.LibraryRepository
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(artistId: String? = null, offset: Int = 0, limit: Int = 50): Result<List<Album>> {
        return libraryRepository.getAlbums(artistId, offset, limit)
    }
}
