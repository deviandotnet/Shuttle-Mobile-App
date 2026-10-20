package com.example.shuttleapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.shuttleapp.ui.theme.ShuttleTheme

/**
 * For screens that don't handle insets themselves yet: paints the status bar Midnight (so its light icons
 * stay legible, as on Home) and keeps the content clear of both system bars. Home, Passes and Sync draw
 * edge to edge and don't use it.
 */
@Composable
fun SystemBarsFrame(content: @Composable () -> Unit) {
    val colors = ShuttleTheme.colors
    Column(Modifier.fillMaxSize().background(colors.canvas)) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(colors.brandBand)
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .consumeWindowInsets(WindowInsets.statusBars)
                .navigationBarsPadding()
        ) { content() }
    }
}
