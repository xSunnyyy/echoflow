package com.plexglassplayer.data.api.service

import com.plexglassplayer.data.api.dto.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PlexApiService {

    // ----------------------------
    // plex.tv endpoints (auth + resources)
    // ----------------------------

    // NOTE: If you're still using the legacy PIN flow, this can remain GET.
    // If Plex changes it on you again, you'll swap to clients.plex.tv + POST later.
    @Headers("Accept: application/json")
    @GET("api/v2/pins")
    suspend fun createPin(
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Header("X-Plex-Product") product: String = "PlexGlassPlayer",
        @Header("X-Plex-Version") version: String = "1.0"
    ): PinResponse

    @Headers("Accept: application/json")
    @GET("api/v2/pins/{pinId}")
    suspend fun checkPin(
        @Path("pinId") pinId: String,
        @Header("X-Plex-Client-Identifier") clientId: String
    ): PinResponse

    /**
     * ✅ IMPORTANT: resources is BEST consumed as JSON list
     * (not MediaContainer XML) so we force JSON + model it correctly.
     */
    @Headers("Accept: application/json")
    @GET("api/v2/resources")
    suspend fun getResources(
        @Header("X-Plex-Token") token: String,
        @Header("X-Plex-Client-Identifier") clientId: String,
        @Query("includeHttps") includeHttps: Int = 1,
        @Query("includeRelay") includeRelay: Int = 1,
        @Query("includeIPv6") includeIPv6: Int = 1
    ): List<DeviceDto>

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
