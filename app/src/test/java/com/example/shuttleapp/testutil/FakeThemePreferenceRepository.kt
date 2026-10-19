package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.domain.repository.ThemePreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeThemePreferenceRepository(initial: ThemeMode = ThemeMode.System) : ThemePreferenceRepository {
    override val themeMode = MutableStateFlow(initial)

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }
}
