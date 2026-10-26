package com.example.shuttleapp.presentation.events

sealed interface QrScannerEvent {
    data object NavigateBack : QrScannerEvent
    /** Haptic + tone so the driver knows the outcome without looking at the screen. */
    data class Feedback(val kind: ScanFeedback) : QrScannerEvent
    data class ShowMessage(val message: String) : QrScannerEvent
}

enum class ScanFeedback { Success, Warning, Error }
