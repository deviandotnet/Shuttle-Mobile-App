package com.example.shuttleapp.domain.usecase.manualaddpassenger

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.usecase.ShuttlePassValidationResult

class ValidateQRCodeInput() {

    fun execute(qrCode: String, qrCodes: List<PassengerQR>) : ShuttlePassValidationResult {


        if(!qrCode.uppercase().contains("Passenger_".uppercase())) { //put !
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


}