package com.plexglassplayer.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AuthCallbackActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        Timber.d("AuthCallback received: $uri")

        // TODO: Handle OAuth callback
        // Extract auth code/token from URI
        // Save to session store
        // Navigate back to main activity

        finish()
    }
}
