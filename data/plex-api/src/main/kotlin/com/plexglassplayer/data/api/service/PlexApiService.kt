package com.plexglassplayer.data.api.service

import com.plexglassplayer.data.api.dto.*
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import retrofit2.http.Path

interface PlexApiService {

    // --- AUTH ---
    @POST("api/v2/pins")
    suspend fun createPin(
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0",
        @Query("strong") strong: Boolean = false
    ): PinResponse

    @GET("api/v2/pins/{pinId}")
    suspend fun checkPin(
        @Path("pinId") pinId: String,
        @Header("X-Plex-Client-Identifier") clientId: String
    ): PinResponse

    @GET("api/v2/resources")
    suspend fun getResources(
        @Header("X-Plex-Token") token: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Query("includeHttps") includeHttps: Int = 1,
        @Query("includeRelay") includeRelay: Int = 1
    ): List<DeviceDto>

    // --- LIBRARY ---
    @GET
    suspend fun getLibrarySections(
        @Url url: String,
        @Query("X-Plex-Token") token: String
    ): LibrarySectionsResponse

    @GET
    suspend fun getArtists(
        @Url url: String,
        @Query("X-Plex-Token") token: String,
        @Query("X-Plex-Container-Start") offset: Int = 0,
        @Query("X-Plex-Container-Size") limit: Int = 50
    ): ArtistsResponse

    @GET
    suspend fun getAlbums(
        @Url url: String,
        @Query("X-Plex-Token") token: String,
        @Query("X-Plex-Container-Start") offset: Int = 0,
        @Query("X-Plex-Container-Size") limit: Int = 50
    ): AlbumsResponse

    @GET
    suspend fun getTracks(
        @Url url: String,
        @Query("X-Plex-Token") token: String,
        @Query("X-Plex-Container-Start") offset: Int = 0,
        @Query("X-Plex-Container-Size") limit: Int = 50
    ): TracksResponse

    @GET
    suspend fun getPlaylists(
        @Url url: String,
        @Query("X-Plex-Token") token: String
    ): PlaylistsResponse

    @GET
    suspend fun search(
        @Url url: String,
        @Query("query") query: String,
        @Query("X-Plex-Token") token: String,
        @Query("X-Plex-Container-Start") offset: Int = 0,
        @Query("X-Plex-Container-Size") limit: Int = 50
    ): TracksResponse

    // --- PLAYLIST ACTIONS ---
    @POST
    suspend fun addToPlaylist(
        @Url url: String,
        @Query("uri") uri: String,
        @Query("X-Plex-Token") token: String
    ): TracksResponse

    @POST
    suspend fun createPlaylist(
        @Url url: String,
        @Query("type") type: String = "audio",
        @Query("title") title: String,
        @Query("smart") smart: Int = 0,
        @Query("uri") uri: String,
        @Query("X-Plex-Token") token: String
    ): TracksResponse

    // --- FIXED: Returns Unit (Retrofit handles errors automatically) ---
    @DELETE
    suspend fun deletePlaylist(
        @Url url: String,
        @Query("X-Plex-Token") token: String
    )
}