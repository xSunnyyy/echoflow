package com.plexglassplayer.data.api.service

import com.plexglassplayer.data.api.dto.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * IMPORTANT:
 * - Retrofit baseUrl for this service MUST be https://plex.tv/
 * - "app.plex.tv" is for browser UI and can return 405 for API calls.
 */
interface PlexApiService {

    // -------------------------------------------------------------------------
    // AUTH (plex.tv)
    // -------------------------------------------------------------------------

    /**
     * Create a PIN for Plex auth.
     *
     * Correct endpoint:
     *   POST https://plex.tv/api/v2/pins?strong=true
     *
     * Plex uses headers for identification, not a JSON body.
     */
    @POST("api/v2/pins")
    suspend fun createPin(
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0.0",
        @Header("X-Plex-Platform") platform: String = "Android",
        @Header("X-Plex-Device") device: String = "Android",
        @Header("X-Plex-Device-Name") deviceName: String = "PlexGlassPlayer",
        @Query("strong") strong: Boolean = true
    ): PinResponse

    /**
     * Poll pin status until Plex returns authToken.
     *
     * Correct endpoint:
     *   GET https://plex.tv/api/v2/pins/{pinId}
     */
    @GET("api/v2/pins/{pinId}")
    suspend fun checkPin(
        @Path("pinId") pinId: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0.0"
    ): PinResponse

    // -------------------------------------------------------------------------
    // ACCOUNT / RESOURCES (plex.tv)
    // -------------------------------------------------------------------------

    /**
     * Fetch available servers/resources for the authenticated user.
     *
     * GET https://plex.tv/api/v2/resources?includeHttps=1&includeRelay=1
     */
    @GET("api/v2/resources")
    suspend fun getResources(
        @Header("X-Plex-Token") token: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0.0",
        @Query("includeHttps") includeHttps: Int = 1,
        @Query("includeRelay") includeRelay: Int = 1
    ): ResourcesResponse

    // -------------------------------------------------------------------------
    // SERVER API (dynamic base URL via @Url)
    // NOTE: For server calls, we pass token as query param (?X-Plex-Token=...)
    // -------------------------------------------------------------------------

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

    /**
     * Plex search is typically against /hubs/search or /search depending on your API usage.
     * If you’re using library search, keep this dynamic.
     */
    @GET
    suspend fun search(
        @Url url: String,
        @Query("query") query: String,
        @Query("X-Plex-Token") token: String,
        @Query("X-Plex-Container-Start") offset: Int = 0,
        @Query("X-Plex-Container-Size") limit: Int = 50
    ): TracksResponse
}
