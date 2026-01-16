package com.plexglassplayer.core.db.di

import android.content.Context
import androidx.room.Room
import com.plexglassplayer.core.db.PlexDatabase
import com.plexglassplayer.core.db.dao.DownloadDao
import com.plexglassplayer.core.db.dao.HomeSectionDao
import com.plexglassplayer.core.db.dao.RecentPlayDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PlexDatabase {
        return Room.databaseBuilder(
            context,
            PlexDatabase::class.java,
            PlexDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDownloadDao(database: PlexDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    fun provideRecentPlayDao(database: PlexDatabase): RecentPlayDao {
        return database.recentPlayDao()
    }

    @Provides
    fun provideHomeSectionDao(database: PlexDatabase): HomeSectionDao {
        return database.homeSectionDao()
    }
}
