package com.plexglassplayer.data.api.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.plexglassplayer.data.api.service.PlexApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlexNetworkModule {

    private const val PLEX_BASE_URL = "https://plex.tv/"

    @Provides
    @Singleton
    fun providePlexJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    /**
     * Plex.tv endpoints ONLY (auth + resources)
     * MUST be https://plex.tv/
     */
    @Provides
    @Singleton
    fun providePlexRetrofit(
        client: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(PLEX_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
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
