package com.example.campushire.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Persists the user's app settings on the device (SharedPreferences).
 * Values are Compose state, so the UI reacts instantly when a setting is changed.
 */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("campushire_settings", Context.MODE_PRIVATE)

    var darkMode by mutableStateOf(prefs.getBoolean("dark", false))
        private set
    var dataSaver by mutableStateOf(prefs.getBoolean("data_saver", false))
        private set
    var notifications by mutableStateOf(prefs.getBoolean("notifications", true))
        private set
    var highContrast by mutableStateOf(prefs.getBoolean("high_contrast", false))
        private set
    var textSize by mutableStateOf(prefs.getString("text_size", "Medium") ?: "Medium")
        private set
    var language by mutableStateOf(prefs.getString("language", "English") ?: "English")
        private set

    val fontScale: Float
        get() = when (textSize) {
            "Small" -> 0.9f
            "Large" -> 1.2f
            else -> 1.0f
        }

    // Names start with update rather than set, avoiding a clash with Kotlin's
    // generated JVM property setters (for example, setDarkMode(boolean)).
    fun updateDarkMode(v: Boolean) { darkMode = v; prefs.edit().putBoolean("dark", v).apply() }
    fun updateDataSaver(v: Boolean) { dataSaver = v; prefs.edit().putBoolean("data_saver", v).apply() }
    fun updateNotifications(v: Boolean) { notifications = v; prefs.edit().putBoolean("notifications", v).apply() }
    fun updateHighContrast(v: Boolean) { highContrast = v; prefs.edit().putBoolean("high_contrast", v).apply() }
    fun updateTextSize(v: String) { textSize = v; prefs.edit().putString("text_size", v).apply() }
    fun updateLanguage(v: String) { language = v; prefs.edit().putString("language", v).apply() }
}

