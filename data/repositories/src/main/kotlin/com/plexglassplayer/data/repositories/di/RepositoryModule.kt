package com.plexglassplayer.data.repositories.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

private val Context.serverDataStore: DataStore<Preferences> by preferencesDataStore(name = "server_preferences")

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServerDataStore

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    @ServerDataStore
    fun provideServerDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.serverDataStore
    }
}
