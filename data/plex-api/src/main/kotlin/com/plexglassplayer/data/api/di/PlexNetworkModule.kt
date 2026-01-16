package com.plexglassplayer.data.api.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.plexglassplayer.data.api.service.PlexApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
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
    @PlexTvJson
    fun providePlexJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    @PlexTvLogging
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // Flip to BODY while debugging auth issues
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    @Provides
    @Singleton
    fun providePlexHeadersInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val req = original.newBuilder()
                // Plex is picky; these help with consistent responses
                .header("Accept", "application/json")
                .build()
            chain.proceed(req)
        }
    }

    @Provides
    @Singleton
    @PlexTvOkHttp
    fun providePlexOkHttpClient(
        @PlexTvLogging logging: HttpLoggingInterceptor,
        plexHeaders: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(plexHeaders)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @PlexTv
    fun providePlexRetrofit(
        @PlexTvOkHttp client: OkHttpClient,
        @PlexTvJson json: Json
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
        @PlexTv retrofit: Retrofit
    ): PlexApiService {
        return retrofit.create(PlexApiService::class.java)
    }
}
