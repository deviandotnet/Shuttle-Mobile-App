package com.example.shuttleapp.presentation.mapper

import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.usecase.shuttlepass.BoardingResult
import com.example.shuttleapp.presentation.state.ScanResultUi
import com.example.shuttleapp.presentation.state.ScannerTripUi
import com.example.shuttleapp.util.formatTimeHHmm

/** Pure formatting for the QR scanner screen. */
object QrScannerUiMapper {

    const val RECENT_ROWS = 3

    fun toTrip(pass: ShuttlePass, routeName: String): ScannerTripUi {
        val card = CurrentPassUiMapper.toCard(pass, PassNames(route = routeName))
        return ScannerTripUi(
            routeLabel = "${card.fromLabel} → ${card.toLabel}",
            timeLabel = if (card.hasDeparture) "Departed ${card.departure}" else "Not departed"
        )
    }

    /** Null for a failure, which is reported as a message instead of a result card. */
    fun toResult(result: BoardingResult): ScanResultUi? = when (result) {
        is BoardingResult.Boarded -> ScanResultUi.Boarded(
            passengerId = result.passenger.id,
            code = shortCode(result.passenger.scannedQR),
            time = formatTimeHHmm(result.passenger.timeIn),
            number = result.number
        )
        is BoardingResult.AlreadyOnBoard -> ScanResultUi.AlreadyOnBoard(
            code = shortCode(result.passenger.scannedQR),
            time = formatTimeHHmm(result.passenger.timeIn),
            number = result.number
        )
        BoardingResult.NotPassenger -> ScanResultUi.NotPassenger
        is BoardingResult.Failed -> null
    }

    /** Encrypted ID payloads are long; the result card shows the start and end so it stays on one line. */
    fun shortCode(code: String): String =
        if (code.length <= SHORT_CODE_MAX) code else "${code.take(SHORT_CODE_HEAD)}…${code.takeLast(SHORT_CODE_TAIL)}"

    private const val SHORT_CODE_MAX = 20
    private const val SHORT_CODE_HEAD = 12
    private const val SHORT_CODE_TAIL = 6
}
