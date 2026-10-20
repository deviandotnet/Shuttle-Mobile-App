package com.example.shuttleapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.presentation.components.ThemeModeSelector
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.viewmodel.AppearanceViewModel
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import com.example.shuttleapp.ui.theme.ShuttleType

@Composable
fun SettingsRoot(viewModel: AppearanceViewModel, onBack: () -> Unit) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    SettingsScreen(themeMode = themeMode, onThemeModeChange = viewModel::onThemeModeChange, onBack = onBack)
}

@Composable
fun SettingsScreen(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit, onBack: () -> Unit) {
    val colors = ShuttleTheme.colors
    Scaffold(
        topBar = { TopBarComponent(title = "Settings", onBack = onBack) },
        containerColor = colors.canvas
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.PageMarginNarrow),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            Text("Display", style = ShuttleType.Section, color = colors.textPrimary)
            Text(
                "Choose how the app looks. System follows your device setting.",
                style = ShuttleType.Body,
                color = colors.textSecondary
            )
            ThemeModeSelector(
                selected = themeMode,
                onSelect = onThemeModeChange,
                modifier = Modifier
                    .padding(top = Dimens.SpaceS)
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(Dimens.RadiusCard))
                    .padding(Dimens.SpaceM)
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun SettingsScreenPreview() = ShuttleAppTheme(darkTheme = false) {
    SettingsScreen(themeMode = ThemeMode.System, onThemeModeChange = {}, onBack = {})
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun SettingsScreenDarkPreview() = ShuttleAppTheme(darkTheme = true) {
    SettingsScreen(themeMode = ThemeMode.Dark, onThemeModeChange = {}, onBack = {})
}
