package com.example.shuttleapp.presentation.viewmodel

import app.cash.turbine.test
import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.testutil.FakeThemePreferenceRepository
import com.example.shuttleapp.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppearanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `exposes the stored theme mode`() = runTest {
        val viewModel = AppearanceViewModel(FakeThemePreferenceRepository(ThemeMode.Dark))
        assertEquals(ThemeMode.Dark, viewModel.themeMode.value)
    }

    @Test
    fun `changing the mode saves it and updates state`() = runTest {
        val repository = FakeThemePreferenceRepository()
        val viewModel = AppearanceViewModel(repository)

        viewModel.themeMode.test {
            assertEquals(ThemeMode.System, awaitItem())
            viewModel.onThemeModeChange(ThemeMode.Light)
            assertEquals(ThemeMode.Light, awaitItem())
            viewModel.onThemeModeChange(ThemeMode.Dark)
            assertEquals(ThemeMode.Dark, awaitItem())
        }
        assertEquals(ThemeMode.Dark, repository.themeMode.value)
    }
}
