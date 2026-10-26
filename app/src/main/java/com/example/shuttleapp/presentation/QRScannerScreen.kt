package com.example.shuttleapp.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shuttleapp.presentation.components.ObserveAsEvents
import com.example.shuttleapp.presentation.components.StyledSnackBarHost
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.components.scanner.CameraAccessOff
import com.example.shuttleapp.presentation.components.scanner.QrCameraPreview
import com.example.shuttleapp.presentation.components.scanner.ScannerSheet
import com.example.shuttleapp.presentation.components.scanner.ScannerViewfinder
import com.example.shuttleapp.presentation.components.scanner.rememberScanFeedback
import com.example.shuttleapp.presentation.events.QrScannerAction
import com.example.shuttleapp.presentation.events.QrScannerEvent
import com.example.shuttleapp.presentation.state.CameraAccess
import com.example.shuttleapp.presentation.state.QrScannerState
import com.example.shuttleapp.presentation.viewmodel.QrScannerViewModel
import com.example.shuttleapp.ui.theme.Dimens
import com.example.shuttleapp.ui.theme.ShuttleTheme
import kotlinx.coroutines.launch

/**
 * Wires [QrScannerViewModel] to the screen: camera permission, the live camera feed, and one-time events
 * (feedback, messages, navigation). Requesting the permission and opening settings need an Activity, so
 * those two actions are handled here and never reach the ViewModel.
 */
@Composable
fun QrScannerRoot(viewModel: QrScannerViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val feedback = rememberScanFeedback()

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onAction(QrScannerAction.CameraPermissionResult(granted))
    }
    LaunchedEffect(Unit) {
        if (!context.hasCameraPermission()) permission.launch(Manifest.permission.CAMERA)
    }
    // Also catches the driver granting access in system settings and coming back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (context.hasCameraPermission()) viewModel.onAction(QrScannerAction.CameraPermissionResult(true))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            QrScannerEvent.NavigateBack -> onBack()
            is QrScannerEvent.Feedback -> feedback(event.kind)
            is QrScannerEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message, actionLabel = "Dismiss", duration = SnackbarDuration.Short)
            }
        }
    }

    QrScannerScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                QrScannerAction.RequestCamera -> permission.launch(Manifest.permission.CAMERA)
                QrScannerAction.OpenAppSettings -> context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                )
                else -> viewModel.onAction(action)
            }
        },
        cameraFeed = { window ->
            QrCameraPreview(
                torchOn = state.torchOn,
                scanWindow = window,
                onQrScanned = { viewModel.onAction(QrScannerAction.QrScanned(it)) },
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

/** Stateless scanner screen (mockups 14–14d). [cameraFeed] is the live camera; previews pass a stand-in. */
@Composable
fun QrScannerScreen(
    state: QrScannerState,
    snackbarHostState: SnackbarHostState,
    onAction: (QrScannerAction) -> Unit,
    cameraFeed: @Composable (scanWindow: Rect) -> Unit
) {
    val colors = ShuttleTheme.colors
    Scaffold(
        containerColor = colors.canvas,
        snackbarHost = { StyledSnackBarHost(snackbarHostState) },
        topBar = { TopBarComponent(title = "Scan passengers", onBack = { onAction(QrScannerAction.Back) }) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Box(Modifier.weight(1f).fillMaxWidth().background(colors.brandBand)) {
                when (state.camera) {
                    CameraAccess.Granted -> ScannerViewfinder(state.result, feed = cameraFeed)
                    CameraAccess.Denied -> CameraAccessOff(onAction)
                    CameraAccess.Unknown -> Unit
                }
            }
            // The sheet rides up over the bottom of the feed, as in the mockups.
            ScannerSheet(state, onAction, Modifier.offset(y = -Dimens.SpaceL))
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
