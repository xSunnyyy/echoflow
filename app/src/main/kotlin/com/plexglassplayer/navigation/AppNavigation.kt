package com.plexglassplayer.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.plexglassplayer.data.auth.AuthRepository
import com.plexglassplayer.feature.auth.AuthScreen
import com.plexglassplayer.feature.home.HomeScreen
import com.plexglassplayer.feature.library.AlbumListScreen
import com.plexglassplayer.feature.library.ArtistListScreen
import com.plexglassplayer.feature.library.TrackListScreen
import com.plexglassplayer.feature.playback.MiniPlayer
import com.plexglassplayer.feature.playback.NowPlayingScreen
import com.plexglassplayer.feature.playback.PlaybackManager
import com.plexglassplayer.feature.search.SearchScreen
import com.plexglassplayer.feature.server.ServerSelectionScreen
import javax.inject.Inject

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = hiltViewModel<AppNavigationViewModel>().authRepository,
    playbackManager: PlaybackManager = hiltViewModel<AppNavigationViewModel>().playbackManager
) {
    val navController = rememberNavController()
    val session by authRepository.sessionFlow.collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide mini-player on certain screens
    val hideMiniPlayer = currentRoute == Screen.NowPlaying.route
        || currentRoute == Screen.Auth.route
        || currentRoute == Screen.ServerSelection.route

    // Determine start destination based on auth state
    val startDestination = when {
        session == null -> Screen.Auth.route
        else -> Screen.Home.route
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
        // Auth flow
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.ServerSelection.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Server selection
        composable(Screen.ServerSelection.route) {
            ServerSelectionScreen(
                onServerSelected = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ServerSelection.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                onLibraryClick = {
                    navController.navigate(Screen.ArtistList.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onSettingsClick = {
                    // TODO: Navigate to settings
                }
            )
        }

        // Library - Artists
        composable(Screen.ArtistList.route) {
            ArtistListScreen(
                onArtistClick = { artist ->
                    navController.navigate(Screen.AlbumList.createRoute(artist.id))
                }
            )
        }

        // Library - Albums for an artist
        composable(
            route = Screen.AlbumList.route,
            arguments = listOf(
                navArgument("artistId") { type = NavType.StringType }
            )
        ) {
            AlbumListScreen(
                onBackClick = { navController.popBackStack() },
                onAlbumClick = { album ->
                    navController.navigate(Screen.TrackList.createRoute(album.id))
                }
            )
        }

        // Library - Tracks in an album
        composable(
            route = Screen.TrackList.route,
            arguments = listOf(
                navArgument("albumId") { type = NavType.StringType }
            )
        ) {
            TrackListScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Search
        composable(Screen.Search.route) {
            SearchScreen()
        }

        // Now Playing
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(
                onBackClick = { navController.popBackStack() },
                playbackManager = playbackManager
            )
        }
        }

        // Mini-player at the bottom
        if (!hideMiniPlayer) {
            MiniPlayer(
                playbackManager = playbackManager,
                onExpandClick = {
                    navController.navigate(Screen.NowPlaying.route)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// Navigation helper ViewModel to inject dependencies
@dagger.hilt.android.lifecycle.HiltViewModel
class AppNavigationViewModel @javax.inject.Inject constructor(
    val authRepository: AuthRepository,
    val playbackManager: PlaybackManager
) : androidx.lifecycle.ViewModel()

// Screen destinations
sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object ServerSelection : Screen("serverSelection")
    data object Home : Screen("home")
    data object ArtistList : Screen("artists")
    data object AlbumList : Screen("albums/{artistId}") {
        fun createRoute(artistId: String) = "albums/$artistId"
    }
    data object TrackList : Screen("tracks/{albumId}") {
        fun createRoute(albumId: String) = "tracks/$albumId"
    }
    data object Search : Screen("search")
    data object NowPlaying : Screen("nowPlaying")
}
