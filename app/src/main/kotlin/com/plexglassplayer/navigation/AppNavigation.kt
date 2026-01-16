package com.plexglassplayer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plexglassplayer.feature.auth.WelcomeScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier
    ) {
        composable("welcome") {
            WelcomeScreen(
                onSignInClick = {
                    // TODO: Navigate to OAuth flow
                }
            )
        }

        // TODO: Add more screens
        // composable("serverSelection") { ServerSelectionScreen(...) }
        // composable("home") { HomeScreen(...) }
        // composable("library") { LibraryScreen(...) }
        // etc.
    }
}
