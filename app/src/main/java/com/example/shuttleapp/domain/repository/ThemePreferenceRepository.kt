package com.example.shuttleapp.domain.repository

import com.example.shuttleapp.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/** The user's chosen [ThemeMode], persisted across restarts. */
interface ThemePreferenceRepository {
    val themeMode: StateFlow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
