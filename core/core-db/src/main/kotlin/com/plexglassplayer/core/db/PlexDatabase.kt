package com.plexglassplayer.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.plexglassplayer.core.db.dao.DownloadDao
import com.plexglassplayer.core.db.dao.HomeSectionDao
import com.plexglassplayer.core.db.dao.RecentPlayDao
import com.plexglassplayer.core.db.entity.DownloadDbEntity
import com.plexglassplayer.core.db.entity.HomeSectionDbEntity
import com.plexglassplayer.core.db.entity.RecentPlayDbEntity

@Database(
    entities = [
        DownloadDbEntity::class,
        RecentPlayDbEntity::class,
        HomeSectionDbEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PlexDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun recentPlayDao(): RecentPlayDao
    abstract fun homeSectionDao(): HomeSectionDao

    companion object {
        const val DATABASE_NAME = "plex_glass_player.db"
    }
}
