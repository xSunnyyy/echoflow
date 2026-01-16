package com.plexglassplayer.data.cache

import com.plexglassplayer.core.db.dao.RecentPlayDao
import com.plexglassplayer.core.db.entity.toDbEntity
import com.plexglassplayer.core.db.entity.toModel
import com.plexglassplayer.core.model.RecentPlay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentPlayCache @Inject constructor(
    private val recentPlayDao: RecentPlayDao
) {

    fun getRecentPlays(limit: Int = 50): Flow<List<RecentPlay>> {
        return recentPlayDao.getRecentPlays(limit).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun addRecentPlay(recentPlay: RecentPlay) {
        recentPlayDao.insertRecentPlay(recentPlay.toDbEntity())
    }

    suspend fun clearOldPlays(beforeEpochMs: Long) {
        recentPlayDao.deleteOldPlays(beforeEpochMs)
    }
}
