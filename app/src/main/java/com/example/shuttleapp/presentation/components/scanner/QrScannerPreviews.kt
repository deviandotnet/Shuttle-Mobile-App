package com.example.shuttleapp.presentation.components.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.shuttleapp.presentation.QrScannerScreen
import com.example.shuttleapp.presentation.state.CameraAccess
import com.example.shuttleapp.presentation.state.PassengerRowUi
import com.example.shuttleapp.presentation.state.QrScannerState
import com.example.shuttleapp.presentation.state.ScanResultUi
import com.example.shuttleapp.presentation.state.ScannerTripUi
import com.example.shuttleapp.ui.theme.ShuttleAppTheme

private val rows = listOf(
    PassengerRowUi("6", 6, "Passenger_15125112", "16:24"),
    PassengerRowUi("5", 5, "Passenger_15124987", "16:22"),
    PassengerRowUi("4", 4, "Passenger_15120034", "16:21")
)

private val scanning = QrScannerState(
    trip = ScannerTripUi("Villa Estrella → Drop-off point", "Departed 16:30"),
    onBoardCount = 6,
    recentRows = rows,
    camera = CameraAccess.Granted
)

private val boarded = scanning.copy(
    onBoardCount = 7,
    recentRows = listOf(PassengerRowUi("7", 7, "Passenger_15125113", "16:26")) + rows.take(2),
    highlightedRowId = "7",
    result = ScanResultUi.Boarded("7", "Passenger_15125113", "16:26", 7),
    torchOn = true
)

private val duplicate = scanning.copy(result = ScanResultUi.AlreadyOnBoard("Passenger_15118876", "16:19", 3))
private val invalid = scanning.copy(result = ScanResultUi.NotPassenger, torchOn = true)
private val denied = scanning.copy(camera = CameraAccess.Denied)

/** Stand-in for the camera: a dim cabin gradient. */
private val fakeFeed: @Composable (Rect) -> Unit = {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF3B4A63), Color(0xFF1F2A44), Color(0xFF101A33)))))
}

@Composable
private fun Screen(state: QrScannerState, dark: Boolean = false) = ShuttleAppTheme(darkTheme = dark) {
    QrScannerScreen(state, remember { SnackbarHostState() }, onAction = {}, cameraFeed = fakeFeed)
}

@Preview(name = "14 Scanning", widthDp = 412, heightDp = 915)
@Composable
private fun ScanningPreview() = Screen(scanning)

@Preview(name = "14a Boarded", widthDp = 412, heightDp = 915)
@Composable
private fun BoardedPreview() = Screen(boarded)

@Preview(name = "14b Already on board", widthDp = 412, heightDp = 915)
@Composable
private fun DuplicatePreview() = Screen(duplicate)

@Preview(name = "14c Not recognised", widthDp = 412, heightDp = 915)
@Composable
private fun InvalidPreview() = Screen(invalid)

@Preview(name = "14d Camera access off", widthDp = 412, heightDp = 915)
@Composable
private fun DeniedPreview() = Screen(denied)

@Preview(name = "14a Boarded, small phone", widthDp = 360, heightDp = 640)
@Composable
private fun BoardedSmallPreview() = Screen(boarded)

@Preview(name = "14a Boarded dark", widthDp = 412, heightDp = 915)
@Composable
private fun BoardedDarkPreview() = Screen(boarded, dark = true)
