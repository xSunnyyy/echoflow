package com.plexglassplayer.data.api.di

import com.plexglassplayer.data.api.service.PlexApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlexNetworkModule {

    /**
     * Plex uses XML responses for most endpoints
     */
    @Provides
    @Singleton
    fun providePlexRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            // 🔥 THIS IS CRITICAL — MUST be plex.tv
            .baseUrl("https://plex.tv/")
            .client(client)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePlexApiService(
        retrofit: Retrofit
    ): PlexApiService {
        return retrofit.create(PlexApiService::class.java)
    }
}
