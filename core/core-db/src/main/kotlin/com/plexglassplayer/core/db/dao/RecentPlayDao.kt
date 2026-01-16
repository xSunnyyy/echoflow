package com.plexglassplayer.core.db.dao

import androidx.room.*
import com.plexglassplayer.core.db.entity.RecentPlayDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayDao {

    @Query("SELECT * FROM recent_plays ORDER BY playedAtEpochMs DESC LIMIT :limit")
    fun getRecentPlays(limit: Int = 50): Flow<List<RecentPlayDbEntity>>

    @Query("SELECT * FROM recent_plays WHERE serverId = :serverId ORDER BY playedAtEpochMs DESC LIMIT :limit")
    fun getRecentPlaysByServer(serverId: String, limit: Int = 50): Flow<List<RecentPlayDbEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentPlay(recentPlay: RecentPlayDbEntity)

    @Query("DELETE FROM recent_plays WHERE playedAtEpochMs < :beforeEpochMs")
    suspend fun deleteOldPlays(beforeEpochMs: Long)

    @Query("DELETE FROM recent_plays")
    suspend fun deleteAll()
}
