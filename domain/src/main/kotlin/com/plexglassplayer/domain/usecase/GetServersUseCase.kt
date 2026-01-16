package com.plexglassplayer.domain.usecase

import com.plexglassplayer.core.model.PlexServer
import com.plexglassplayer.core.util.Result
import com.plexglassplayer.data.repositories.ServerRepository
import javax.inject.Inject

class GetServersUseCase @Inject constructor(
    private val serverRepository: ServerRepository
) {
    suspend operator fun invoke(): Result<List<PlexServer>> {
        return serverRepository.getServers()
    }
}
