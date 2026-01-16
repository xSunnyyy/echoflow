package com.plexglassplayer.data.api.di

import com.plexglassplayer.data.api.service.PlexApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun providePlexApiService(
        retrofit: Retrofit
    ): PlexApiService {
        return retrofit.create(PlexApiService::class.java)
    }
}
