package com.example.shuttleapp.presentation.events

sealed interface QrScannerAction {
    data class QrScanned(val rawValue: String) : QrScannerAction
    data class CameraPermissionResult(val granted: Boolean) : QrScannerAction
    data object ToggleTorch : QrScannerAction
    data object Done : QrScannerAction
    data object Back : QrScannerAction

    // Handled by the Root composable (they need an Activity), never reach the ViewModel.
    data object RequestCamera : QrScannerAction
    data object OpenAppSettings : QrScannerAction
}
