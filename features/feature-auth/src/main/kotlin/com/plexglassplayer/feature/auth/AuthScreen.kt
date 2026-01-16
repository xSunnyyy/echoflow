package com.plexglassplayer.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plexglassplayer.core.ui.components.*
import com.plexglassplayer.data.auth.PinAuthData

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is AuthUiState.Idle -> {
                WelcomeContent(
                    onSignInClick = { viewModel.startAuth() }
                )
            }

            is AuthUiState.Loading -> {
                LoadingState(message = "Preparing authentication...")
            }

            is AuthUiState.PinGenerated -> {
                PinGeneratedContent(
                    pinData = state.pinData,
                    onOpenBrowser = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.pinData.authUrl))
                        context.startActivity(intent)
                    }
                )
            }

            is AuthUiState.Success -> {
                SuccessContent()
            }

            is AuthUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

@Composable
private fun WelcomeContent(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "Plex Glass Player",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Stream your music from Plex with a beautiful frosted-glass interface",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassButton(
                    text = "Sign in with Plex",
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PinGeneratedContent(
    pinData: PinAuthData,
    onOpenBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var browserOpened by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!browserOpened) {
            onOpenBrowser()
            browserOpened = true
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "Sign in to Plex",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter this PIN at plex.tv/link",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = pinData.pinCode,
                        style = MaterialTheme.typography.displayMedium.copy(
                            letterSpacing = 8.sp
                        ),
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Waiting for authorization...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlassButton(
                    text = "Open browser",
                    onClick = onOpenBrowser,
                    modifier = Modifier.fillMaxWidth(),
                    style = GlassButtonStyle.SECONDARY
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Successfully signed in!",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
