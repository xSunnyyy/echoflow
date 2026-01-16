package com.plexglassplayer.core.db.dao

import androidx.room.*
import com.plexglassplayer.core.db.entity.DownloadDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAtEpochMs DESC")
    fun getAllDownloads(): Flow<List<DownloadDbEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAtEpochMs DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadDbEntity>>

    @Query("SELECT * FROM downloads WHERE serverId = :serverId ORDER BY createdAtEpochMs DESC")
    fun getDownloadsByServer(serverId: String): Flow<List<DownloadDbEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadDbEntity?

    @Query("SELECT * FROM downloads WHERE serverId = :serverId AND trackId = :trackId AND status = 'COMPLETED' LIMIT 1")
    suspend fun findCompletedDownload(serverId: String, trackId: String): DownloadDbEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadDbEntity)

    @Update
    suspend fun updateDownload(download: DownloadDbEntity)

    @Query("UPDATE downloads SET status = :status, progressPct = :progress, bytesDownloaded = :bytesDownloaded, updatedAtEpochMs = :updatedAt WHERE id = :id")
    suspend fun updateProgress(id: String, status: String, progress: Int, bytesDownloaded: Long, updatedAt: Long)

    @Query("UPDATE downloads SET status = 'COMPLETED', filePath = :filePath, progressPct = 100, updatedAtEpochMs = :updatedAt WHERE id = :id")
    suspend fun markCompleted(id: String, filePath: String, updatedAt: Long)

    @Query("UPDATE downloads SET status = 'FAILED', errorMessage = :errorMessage, updatedAtEpochMs = :updatedAt WHERE id = :id")
    suspend fun markFailed(id: String, errorMessage: String, updatedAt: Long)

    @Delete
    suspend fun deleteDownload(download: DownloadDbEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun deleteAllCompleted()
}
