package com.plexglassplayer.data.api.di

import com.plexglassplayer.data.api.service.PlexApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlexNetworkModule {

    private const val PLEX_BASE_URL = "https://plex.tv/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    /**
     * IMPORTANT:
     * - This Retrofit is ONLY for plex.tv endpoints (auth + resources)
     * - We use ScalarsConverterFactory so this module compiles immediately.
     *
     * If your DTOs are JSON via Kotlinx serialization, we can swap to that converter next.
     * If your DTOs are XML (Plex default), we can swap to SimpleXml next.
     */
    @Provides
    @Singleton
    fun providePlexRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PLEX_BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePlexApiService(retrofit: Retrofit): PlexApiService {
        return retrofit.create(PlexApiService::class.java)
    }
}
