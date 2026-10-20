package com.example.shuttleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.presentation.viewmodel.MainViewModel
import com.example.shuttleapp.domain.model.ThemeMode
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.ShuttleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(statusBarStyle = BRAND_BAND_STATUS_BAR)
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            // Status bar icons stay light: every screen puts a Midnight band behind them. The navigation bar
            // follows the app theme (not the device's), so its icons stay legible over the canvas.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = BRAND_BAND_STATUS_BAR,
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }
            // Screens own their insets: Home, Passes and Sync draw edge to edge, the rest use SystemBarsFrame.
            Box(Modifier.fillMaxSize()) {

                ShuttleAppTheme(darkTheme = darkTheme) {
                    val session by mainViewModel.session.collectAsStateWithLifecycle()

                    // Resolved once: later sign-in/sign-out navigation is done by the screens themselves.
                    var startDestination by rememberSaveable { mutableStateOf<String?>(null) }
                    if (startDestination == null) {
                        startDestination = when (val load = session) {
                            SessionLoad.Loading -> null
                            SessionLoad.SignedOut -> Route.LoginScreen.route
                            is SessionLoad.SignedIn -> Route.HomeScreen.route + "/${load.session.accountId}"
                        }
                    }

                    // Gives every screen the themed canvas and default text color, including those without a Scaffold.
                    Surface(color = ShuttleTheme.colors.canvas, contentColor = ShuttleTheme.colors.textPrimary) {
                        startDestination?.let { Navigation(startDestination = it) } ?: SessionLoading()
                    }
                }
            }
        }

    }
}

private val BRAND_BAND_STATUS_BAR = SystemBarStyle.dark(Color.TRANSPARENT)

@Composable
private fun SessionLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShuttleTheme.colors.brandBand),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ShuttleTheme.colors.onBrandBand)
    }
}
