package com.plexglassplayer.data.repositories

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Reactive stream so UI updates instantly
    private val _userName = MutableStateFlow(prefs.getString(KEY_USERNAME, "User") ?: "User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    companion object {
        private const val KEY_USERNAME = "username"
    }

    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USERNAME, name).apply()
        _userName.value = name
    }
}