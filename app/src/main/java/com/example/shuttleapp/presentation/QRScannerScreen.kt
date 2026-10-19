package com.example.shuttleapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.presentation.components.InformationTextDialog
import com.example.shuttleapp.presentation.components.TopBarComponent
import com.example.shuttleapp.presentation.events.QRInputEvent
import com.example.shuttleapp.presentation.viewmodel.QrAnalyzerViewModel
import com.example.shuttleapp.util.displayTime
import com.example.shuttleapp.ui.theme.ShuttleTheme

@Composable
fun QRScannerScreen(shuttlePassId: String, navController: NavController, qrAnalyzerViewModel: QrAnalyzerViewModel) {
    val colors = ShuttleTheme.colors
    val context = LocalContext.current

    val passenger by qrAnalyzerViewModel.passengerQR.collectAsState()
    val qrInputState by qrAnalyzerViewModel.qrInputState.collectAsState()

    var showErrorMessage by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val scanComplete by qrAnalyzerViewModel.scanComplete.collectAsState()

    var hasCameraPermission by remember {

        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    //The true param means the effect will be launched every time the component is launched.
    LaunchedEffect(true) {

        //request camera permission
        launcher.launch(Manifest.permission.CAMERA)

        //check the current permission status
        val permissionStatus =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        Log.d("Permission", "Current permission status: $permissionStatus")

        // If permission is not granted, request it again
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.CAMERA)
        } else {
            Log.d("QRScannerScreen", "Permission already granted")
        }
    }

    //1) id
    //2) load
    LaunchedEffect(shuttlePassId) {
        qrAnalyzerViewModel.loadPassenger(shuttlePassId)
        qrAnalyzerViewModel.getScannedQRCodes(shuttlePassId)
    }

    LaunchedEffect(passenger) {

        try {
            if (shuttlePassId != "") { //eto yung pag edit mo yung existing eme eme
                //data changes happens here for fetching existing values
                qrAnalyzerViewModel.onQRInputEvent(QRInputEvent.ScannedQRChanged(passenger.scannedQR))
            } else {
                qrAnalyzerViewModel.onQRInputEvent(QRInputEvent.ScannedQRChanged(""))
            }

        } catch (ex: Exception) {
            Log.e("QRScannerScreen", "An error occurred getting values: ${ex.message}")
            ex.printStackTrace()
        }
    }

    //start of UI
    Scaffold(
        topBar = {
            TopBarComponent(navController = navController, title = "Scan QR Code")
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.brandBand.copy(0.9f))
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {

            if (hasCameraPermission) {

                Box(modifier = Modifier.fillMaxSize()) {


                    //if you put here the Camera Preview, it will fill the whole screen
                    //---> <----//

                    //OVERLAY SECTION//

                    //text as an overlay
                    Box(modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp)
                    ) {
                        Text(
                            text= "Place the QR code inside the scanning area",
                            style = TextStyle(
                                color = colors.onBrandBand,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                            ) )
                    }
                    //box indicator as an overlay
                    Box(
                        modifier = Modifier.padding(top = 130.dp)
                            .size(315.dp) // adjust the size as needed
                            .border(1.25.dp, colors.onBrandBand, shape = RoundedCornerShape(20.dp)) // adjust the border width and color as needed
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        //if you put this into the box indicator, the preview will be the size of the box.
                        CameraPreview { performAnalysis ->
                            qrAnalyzerViewModel.qrCodeAnalyzer(performAnalysis)
                        }
                    }

                    //if you want to no buttons and auto scan the qr once detected... use this
                    //(feature 1)

                    LaunchedEffect(scanComplete) {
                        if(scanComplete) {
                            try {
                                if (!qrAnalyzerViewModel.submitScannedPassengerQR()) {
                                    showErrorMessage = true
//                                        qrInputState.qrCodeErrorMessage?.let {
//                                            Toast.makeText(context, qrInputState.qrCodeErrorMessage, Toast.LENGTH_SHORT).show()
//                                            navController.navigateUp()
//                                        }
                                } else {
                                    val passengerQR = PassengerQR(
                                        shuttlePassId = shuttlePassId,
                                        scannedQR = qrInputState.qrCode,
                                        timeIn = displayTime(),
                                        isDraft = true
                                    )
                                    qrAnalyzerViewModel.insertPassengerQR(passengerQR)
                                    showSuccessMessage = true
                                }

                            }
                            catch (ex: Exception) {
                                ex.message.toString()
                            }
                        }
                    }

                    //feature 1 end

                    //feature 2
                    //BUTTON - manual read of qr via trigger events
//                    Box(modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(bottom = 32.dp)
//                        .border(1.25.dp, White, RoundedCornerShape(45.dp))
//                        .clickable {
//                            //one frame
//                            try {
//                                //TODO REFACTOR THIS TO ADDRESS THE DOUBLE CLICK ISSUE WHEN QR IS INVALID
//                                if (!viewModel.submitScannedPassengerQR()) {
//                                    showErrorMessage = true
//                                } else {
//                                    val passengerQR = PassengerQR(
//                                        shuttlePassId = shuttlePassId,
//                                        scannedQR = qrInputState.qrCode,
//                                        timeIn = displayTime(),
//                                        isDraft = true
//                                    )
//                                    viewModel.insertPassengerQR(passengerQR)
//                                    showSuccessMessage = true
//                                }
//
//                            }
//                            catch (ex: Exception) {
//                                ex.message.toString()
//                            }
//                        }
//                    ){
//
//                        Icon(
//                            imageVector = Icons.Outlined.QrCodeScanner,
//                            tint = White,
//                            contentDescription = "Scan QR",
//                            modifier = Modifier.size(70.dp).padding(12.dp)
//                        )
//                    }
                    //feature 2 end

                    //dialog for invalid qr
                    if(showErrorMessage) {
                        InformationTextDialog(
                            onDismissRequest = {
                                qrAnalyzerViewModel.resetQRInputState()
                                showErrorMessage = false
                            },
                            dialogTitle = "Error",
                            dialogText = qrInputState.qrCodeErrorMessage!!,
                            icon = Icons.Default.ErrorOutline,
                            iconTint = colors.error
                        )
                    }

                    if (showSuccessMessage) {
                        InformationTextDialog(
                            onDismissRequest = {
                                //navController.navigateUp()
                                qrAnalyzerViewModel.resetQRInputState()
                                showSuccessMessage = false
                            },
                            dialogTitle = "Success",
                            dialogText = "Passenger boarded successfully",
                            icon = Icons.Default.CheckCircleOutline,
                            iconTint = colors.success
                        )
                    }

                }
            }

        }

    } //scaffold end

}

@Composable
fun CameraPreview(performAnalysis: (ImageProxy) -> Unit) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    AndroidView(
        factory = {
            val previewView = PreviewView(it)
            val preview = Preview.Builder().build()
            val selector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            Log.i("CameraPreview", "Camera preview view created")

            preview.surfaceProvider = previewView.surfaceProvider

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

            Log.i("CameraPreview", "Image analysis builder created")

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(it)) { image ->
                performAnalysis(image)
            }

            Log.i("CameraPreview", "Image analysis analyzer set")

            try {

                Log.i("CameraPreview", "Binding camera provider to lifecycle")

                cameraProviderFuture.get().bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageAnalysis
                )
            } catch (ex: Exception) {
                Log.e("CameraPreview", "Error binding camera provider to lifecycle", ex)
                ex.printStackTrace()
            }
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

}