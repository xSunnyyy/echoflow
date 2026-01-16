package com.plexglassplayer.data.api.service

import com.plexglassplayer.data.api.dto.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface PlexApiService {

    // ----------------------------
    // Auth endpoints (plex.tv)
    // ----------------------------

    /**
     * ✅ MUST be POST (GET = HTTP 405)
     */
    @POST("api/v2/pins")
    suspend fun createPin(
        @Query("strong") strong: Boolean = true,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0.0",
        @Header("X-Plex-Platform") platform: String = "Android",
        @Header("X-Plex-Device") device: String = "Android",
        @Header("X-Plex-Device-Name") deviceName: String = "PlexGlassPlayer"
    ): PinResponse

    @GET("api/v2/pins/{pinId}")
    suspend fun checkPin(
        @Path("pinId") pinId: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0.0"
    ): PinResponse

    @GET("api/v2/resources")
    suspend fun getResources(
        @Header("X-Plex-Token") token: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0.0"
    ): ResourcesResponse


    // ----------------------------
    // Server endpoints (dynamic base URL)
    // ----------------------------

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
}
