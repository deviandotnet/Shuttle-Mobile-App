package com.example.shuttleapp.data.repositoryImpl

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.domain.repository.ThemePreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Stores the theme choice in plain SharedPreferences: it is a UI preference, not sensitive data. */
@Singleton
class ThemePreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemePreferenceRepository {

    private val prefs: SharedPreferences by lazy { context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE) }
    private val _themeMode: MutableStateFlow<ThemeMode> by lazy { MutableStateFlow(read()) }

    override val themeMode: StateFlow<ThemeMode> get() = _themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        withContext(Dispatchers.IO) {
            try {
                prefs.edit { putString(KEY_THEME_MODE, mode.name) }
            } catch (e: Exception) {
                // The choice still applies for this run; it just won't survive a restart.
                Log.e(TAG, "Could not save theme mode", e)
            }
        }
    }

    private fun read(): ThemeMode = try {
        ThemeMode.fromStored(prefs.getString(KEY_THEME_MODE, null))
    } catch (e: Exception) {
        Log.e(TAG, "Could not read theme mode", e)
        ThemeMode.System
    }

    private companion object {
        const val TAG = "ThemePreference"
        const val FILE_NAME = "ui_prefs"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
