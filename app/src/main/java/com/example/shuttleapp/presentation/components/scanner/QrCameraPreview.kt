package com.example.shuttleapp.presentation.components.scanner

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Live camera feed that decodes QR codes with ML Kit's bundled model (no Play services, works offline).
 * Only codes whose centre falls inside [scanWindow] (px, relative to this view) are reported, so stray
 * codes and reflections outside the frame are ignored. Repeats are reported every frame; the caller dedupes.
 */
@Composable
fun QrCameraPreview(
    torchOn: Boolean,
    scanWindow: Rect?,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentWindow by rememberUpdatedState(scanWindow)
    val currentOnQrScanned by rememberUpdatedState(onQrScanned)

    val scanner = remember {
        BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
    }
    val controller = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            val executor = ContextCompat.getMainExecutor(context)
            setImageAnalysisAnalyzer(
                executor,
                MlKitAnalyzer(listOf(scanner), ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED, executor) { result ->
                    result.getThrowable(scanner)?.let { Log.w("QrCameraPreview", "QR decode failed", it) }
                    result.getValue(scanner)
                        ?.firstOrNull { it.isInside(currentWindow) }
                        ?.rawValue
                        ?.let { currentOnQrScanned(it) }
                }
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }
    // The scanner lives as long as this composable (it is remembered without a key), so close it only on
    // leaving the composition; a lifecycle-owner change rebinds the controller to the same open scanner.
    DisposableEffect(Unit) {
        onDispose { scanner.close() }
    }
    LaunchedEffect(torchOn) { controller.enableTorch(torchOn) }

    AndroidView(
        factory = { PreviewView(it).apply { this.controller = controller } },
        modifier = modifier
    )
}

private fun Barcode.isInside(window: Rect?): Boolean {
    val box = boundingBox ?: return false
    return window == null || window.contains(Offset(box.exactCenterX(), box.exactCenterY()))
}
