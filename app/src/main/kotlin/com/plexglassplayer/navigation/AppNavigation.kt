package com.plexglassplayer.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.plexglassplayer.data.auth.AuthRepository
import com.plexglassplayer.feature.auth.AuthScreen
import com.plexglassplayer.feature.downloads.DownloadsScreen
import com.plexglassplayer.feature.home.HomeScreen
import com.plexglassplayer.feature.library.AlbumListScreen
import com.plexglassplayer.feature.library.ArtistListScreen
import com.plexglassplayer.feature.library.TrackListScreen
import com.plexglassplayer.feature.playback.MiniPlayer
import com.plexglassplayer.feature.playback.NowPlayingScreen
import com.plexglassplayer.feature.playback.PlaybackManager
import com.plexglassplayer.feature.search.SearchScreen
import com.plexglassplayer.feature.server.ServerSelectionScreen
import com.plexglassplayer.feature.settings.SettingsScreen
import javax.inject.Inject

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    // FIX: Inject the ViewModel here as a default parameter
    viewModel: AppNavigationViewModel = hiltViewModel()
) {
    // FIX: Access dependencies from the ViewModel inside the function
    val authRepository = viewModel.authRepository
    val playbackManager = viewModel.playbackManager

    val navController = rememberNavController()
    val session by authRepository.sessionFlow.collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide mini-player on specific screens
    val hideMiniPlayer = currentRoute == Screen.NowPlaying.route
            || currentRoute == Screen.Auth.route
            || currentRoute == Screen.ServerSelection.route
            || currentRoute == Screen.Home.route
            || currentRoute == Screen.Queue.route

    val startDestination = when {
        session == null -> Screen.Auth.route
        else -> Screen.Home.route
    }

    Box(modifier = modifier.fillMaxSize()) {
        val miniPlayerHeight = if (!hideMiniPlayer && playbackManager.currentTrack.collectAsState().value != null) 70.dp else 0.dp

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = miniPlayerHeight)
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
                    onLibraryClick = { navController.navigate(Screen.ArtistList.route) },
                    onSeeAllMusicClick = { navController.navigate(Screen.AllMusic.route) },
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onDownloadsClick = { navController.navigate(Screen.Downloads.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onNowPlayingClick = { navController.navigate(Screen.NowPlaying.route) }
                )
            }

            // All Music (Reusing TrackListScreen)
            composable(Screen.AllMusic.route) {
                TrackListScreen(
                    onBackClick = { navController.popBackStack() }
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

            // Library - Albums
            composable(
                route = Screen.AlbumList.route,
                arguments = listOf(navArgument("artistId") { type = NavType.StringType })
            ) {
                AlbumListScreen(
                    onBackClick = { navController.popBackStack() },
                    onAlbumClick = { album ->
                        navController.navigate(Screen.TrackList.createRoute(album.id))
                    }
                )
            }

            // Library - Tracks (Specific Album)
            composable(
                route = Screen.TrackList.route,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType })
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
                    onQueueClick = { navController.navigate(Screen.Queue.route) },
                    playbackManager = playbackManager
                )
            }

            // Queue
            composable(Screen.Queue.route) {
                com.plexglassplayer.feature.playback.QueueScreen(
                    playbackManager = playbackManager,
                    onBackClick = { navController.popBackStack() },
                    onNowPlayingClick = {
                        navController.navigate(Screen.NowPlaying.route)
                    }
                )
            }

            // Downloads
            composable(Screen.Downloads.route) {
                DownloadsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        if (!hideMiniPlayer) {
            MiniPlayer(
                playbackManager = playbackManager,
                onExpandClick = { navController.navigate(Screen.NowPlaying.route) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ViewModel
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
    data object AllMusic : Screen("allMusic")
    data object ArtistList : Screen("artists")
    data object AlbumList : Screen("albums/{artistId}") {
        fun createRoute(artistId: String) = "albums/$artistId"
    }
    data object TrackList : Screen("tracks/{albumId}") {
        fun createRoute(albumId: String) = "tracks/$albumId"
    }
    data object Search : Screen("search")
    data object NowPlaying : Screen("nowPlaying")
    data object Queue : Screen("queue")
    data object Downloads : Screen("downloads")
    data object Settings : Screen("settings")
}