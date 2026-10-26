package com.example.shuttleapp.domain.usecase.manualaddpassenger

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.usecase.ShuttlePassValidationResult

class ValidateQRCodeInput() {

    fun execute(qrCode: String, qrCodes: List<PassengerQR>) : ShuttlePassValidationResult {


        if(!isPassengerQr(qrCode)) {
            return ShuttlePassValidationResult(
                successful = false,
                errorMessage = "Invalid QR Code. Unknown."
            )
        }

        if(qrCodes.any{passenger -> passenger.scannedQR == qrCode}) {
            return ShuttlePassValidationResult(
                successful = false,
                errorMessage = "Passenger was already onboarded"
            )
        }

        return ShuttlePassValidationResult(
            successful = true
        )
    }

    companion object {
        private const val PASSENGER_QR_MARKER = "PASSENGER_"

        /** The one rule for "this is a passenger QR", shared by the scanner and the edit-passenger dialog. */
        fun isPassengerQr(qrCode: String): Boolean = qrCode.uppercase().contains(PASSENGER_QR_MARKER)
    }
}
