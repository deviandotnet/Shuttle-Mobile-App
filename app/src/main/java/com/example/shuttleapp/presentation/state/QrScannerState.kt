package com.example.shuttleapp.presentation.state

/** Everything the QR scanner shows: the camera (or why it's off), the last result, and the docked manifest. */
data class QrScannerState(
    val trip: ScannerTripUi = ScannerTripUi(),
    val onBoardCount: Int = 0,
    /** The newest few boarded passengers, newest first. */
    val recentRows: List<PassengerRowUi> = emptyList(),
    /** The row of the passenger who just boarded, highlighted while the result card shows. */
    val highlightedRowId: String? = null,
    val result: ScanResultUi? = null,
    val camera: CameraAccess = CameraAccess.Unknown,
    val torchOn: Boolean = false
) {
    val torchEnabled: Boolean get() = camera == CameraAccess.Granted
}

data class ScannerTripUi(
    /** "Villa Estrella → Drop-off point". */
    val routeLabel: String = "",
    /** "Departed 16:30" or "Not departed". */
    val timeLabel: String = ""
)

/** The card under the viewfinder after a scan; it clears itself after a few seconds. */
sealed interface ScanResultUi {
    data class Boarded(val passengerId: String, val code: String, val time: String, val number: Int) : ScanResultUi
    data class AlreadyOnBoard(val code: String, val time: String, val number: Int) : ScanResultUi
    data object NotPassenger : ScanResultUi
}

enum class CameraAccess { Unknown, Granted, Denied }
