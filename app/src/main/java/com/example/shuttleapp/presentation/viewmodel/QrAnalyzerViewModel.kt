package com.example.shuttleapp.presentation.viewmodel

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.domain.usecase.manualaddpassenger.ValidateQRCodeInput
import com.example.shuttleapp.presentation.events.QRInputEvent
import com.example.shuttleapp.presentation.state.QRInputState
import com.example.shuttleapp.presentation.state.ScannedQRListState
import com.example.shuttleapp.util.Resource
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import javax.inject.Inject

@HiltViewModel
class QrAnalyzerViewModel @Inject constructor(
    private val passengerQRRepository: PassengerQRRepository,
    private val validateQRCodeInput : ValidateQRCodeInput
) : ViewModel() {

    private var _scannedQRListState = MutableStateFlow(ScannedQRListState())
    val scannedQRListState: StateFlow<ScannedQRListState> get() = _scannedQRListState

    private val _passengerQR = MutableStateFlow(PassengerQR())
    val passengerQR: StateFlow<PassengerQR> get() = _passengerQR

    private val _qrInputState = MutableStateFlow(QRInputState())
    val qrInputState: StateFlow<QRInputState> = _qrInputState.asStateFlow()

    private var _scanComplete = MutableStateFlow(false)
    val scanComplete: StateFlow<Boolean> = _scanComplete.asStateFlow()

    private var _currentQrResult = MutableStateFlow<String?>("")


    //fetch the frame to analyze
    fun qrCodeAnalyzer(image: ImageProxy) {

        viewModelScope.launch(Dispatchers.IO) {
            val multiFormatReader= MultiFormatReader().apply {
                setHints(
                    mapOf(
                        DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                            BarcodeFormat.QR_CODE)
                    )
                )
            }

            val supportedFormats = listOf(
                ImageFormat.YUV_422_888,
                ImageFormat.YUV_420_888,
                ImageFormat.YUV_444_888
            )


            if(image.format in supportedFormats) {

                val bytes = image.planes.first().buffer.toByteArray()

                val source = PlanarYUVLuminanceSource(
                    bytes, image.width, image.height, 0, 0,
                    image.width, image.height, false
                )

                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                //DECODE THE INFORMATION WE GOT IN QR CODE
                //this will examine the qr code\

                try {
                    val result = multiFormatReader.decodeWithState(binaryBitmap)
                    val qrCodeText = result.text

                    if (qrCodeText != _currentQrResult.value) {
                        _currentQrResult.value = qrCodeText
                        onQRInputEvent(QRInputEvent.ScannedQRChanged(qrCodeText))
                        _scanComplete.value = true
                        Log.i("qrCodeAnalyzer", qrCodeText)
                    }

                } catch (_: NotFoundException) {
                    // Expected when no QR is in frame
                } finally {
                    image.close()
                }
            } else {
                Log.i("qrCodeAnalyzer","Image closed")
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also { get(it) }
    }

    fun loadPassenger(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getPassengerById(id).collectLatest{ passenger ->
                    _passengerQR.value = passenger
                }
            }
            catch (ex: Exception) {
                Log.e("QRAnalyzerViewModelLoadPassenger", "An error occurred. Please try again. ${ex.message}")
            }

        }
    }

    fun getPassengerById(id: String) : Flow<PassengerQR> {
        return passengerQRRepository.getPassengerById(id)

    }

    fun getScannedQRCodes(shuttlePassId: String)  {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                passengerQRRepository.getDraftedPassengers(shuttlePassId).collect { passengerQr ->
                    _scannedPassengers.value = passengerQr
                }
            } catch (ex: Exception) {
                Log.e("QVMGetScannedQRCodes", "An error occurred while QR codes: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }


    fun onQRInputEvent(event : QRInputEvent) {
        when(event) {
            is QRInputEvent.ScannedQRChanged -> {
                _qrInputState.value = _qrInputState.value.copy(qrCode = event.scannedQR)
            }
            is QRInputEvent.Submit -> {
                submitScannedPassengerQR()
            }
        }
    }
    //for validation
    private val _scannedPassengers = MutableStateFlow<List<PassengerQR>>(emptyList())

    //TODO pass list here for validation for employee number and department, (use hashset
    fun submitScannedPassengerQR() : Boolean {

        val qrCodeResult = validateQRCodeInput.execute(_qrInputState.value.qrCode, _scannedPassengers.value) //<<-- here i Will use here

        val hasError = listOf(
            qrCodeResult,
        ).any {!it.successful}

        if(hasError) {
            _qrInputState.value = _qrInputState.value.copy(
                qrCodeErrorMessage = qrCodeResult.errorMessage,
            )
            return false
        }

        return true
    }

    fun resetQRInputState() {
        _qrInputState.value = QRInputState()
        _scanComplete.value = false
    }


    fun insertPassengerQR(passengerQR: PassengerQR) {

        viewModelScope.launch(Dispatchers.IO) {
            passengerQRRepository.insertPassenger(passengerQR)
        }
    }


} //viewmodel end . . .
