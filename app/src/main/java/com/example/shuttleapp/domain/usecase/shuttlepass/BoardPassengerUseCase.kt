package com.example.shuttleapp.domain.usecase.shuttlepass

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import com.example.shuttleapp.domain.usecase.manualaddpassenger.ValidateQRCodeInput
import com.example.shuttleapp.util.displayTime
import kotlinx.coroutines.CancellationException

sealed interface BoardingResult {
    /** [number] is the passenger's place in boarding order (1 = first aboard). */
    data class Boarded(val passenger: PassengerQR, val number: Int) : BoardingResult
    data class AlreadyOnBoard(val passenger: PassengerQR, val number: Int) : BoardingResult
    data object NotPassenger : BoardingResult
    data class Failed(val message: String) : BoardingResult
}

/** Boards one scanned QR onto a draft pass, or says why it can't be (not a passenger QR, already aboard). */
class BoardPassengerUseCase(
    private val passengerQRRepository: PassengerQRRepository,
    private val now: () -> String = { displayTime() }
) {

    suspend operator fun invoke(passId: String, rawQr: String, onBoard: List<PassengerQR>): BoardingResult {
        if (!ValidateQRCodeInput.isPassengerQr(rawQr)) return BoardingResult.NotPassenger

        // onBoard is in boarding order, the same order the manifest numbers (CurrentPassUiMapper.toRows),
        // so "#3" here matches row 3 there.
        val existing = onBoard.indexOfFirst { it.scannedQR == rawQr }
        if (existing >= 0) return BoardingResult.AlreadyOnBoard(onBoard[existing], existing + 1)

        val passenger = PassengerQR(shuttlePassId = passId, scannedQR = rawQr, timeIn = now(), isDraft = true)
        return try {
            passengerQRRepository.insertPassenger(passenger)
            BoardingResult.Boarded(passenger, onBoard.size + 1)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            BoardingResult.Failed("Couldn't save the passenger. ${ex.message.orEmpty()}".trim())
        }
    }
}
