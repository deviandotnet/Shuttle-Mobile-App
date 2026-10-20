package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.domain.repository.ThemePreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the appearance picker on the Settings screen. MainActivity observes the same repository and re-themes. */
@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val themePreferenceRepository: ThemePreferenceRepository
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themePreferenceRepository.themeMode

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { themePreferenceRepository.setThemeMode(mode) }
    }
}
