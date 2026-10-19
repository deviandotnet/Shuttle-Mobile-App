package com.example.shuttleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.presentation.viewmodel.MainViewModel
import com.example.shuttleapp.ui.theme.NavyBlue
import com.example.shuttleapp.ui.theme.ShuttleAppTheme
import com.example.shuttleapp.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            Box(Modifier.Companion.systemBarsPadding()) {

                ShuttleAppTheme {
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

                    startDestination?.let { Navigation(startDestination = it) } ?: SessionLoading()
                }
            }
        }

    }
}

@Composable
private fun SessionLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = White)
    }
}
