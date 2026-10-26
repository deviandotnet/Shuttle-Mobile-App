package com.example.shuttleapp.domain.usecase.shuttlepass

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.displayTime
import java.util.Calendar

/**
 * Ends a draft pass: stamps the arrival time, flags it late, and finalizes it with its passengers
 * so it becomes eligible for sync.
 */
class CompleteShuttlePassUseCase(
    private val shuttlePassRepository: ShuttlePassRepository,
    private val now: () -> Calendar = { Calendar.getInstance() }
) {

    suspend operator fun invoke(pass: ShuttlePass, passengers: List<PassengerQR>): Resource<Unit> {
        val time = now()
        val finalPass = pass.copy(
            arrival = displayTime(time.time),
            isLateShuttle = isLateShuttle(pass.tripType, time)
        )
        return shuttlePassRepository.finalizeDraft(finalPass.driver, finalPass, passengers)
    }

    companion object {
        private const val INCOMING_LATE_FROM = 6 * 60 + 55   // 6:55 AM
        private const val OUTGOING_LATE_FROM = 18 * 60 + 55  // 6:55 PM

        /** Incoming is late from 6:55 AM, outgoing from 6:55 PM, until midnight. */
        fun isLateShuttle(tripType: String, currentTime: Calendar): Boolean {
            val minutesOfDay = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
            return when (tripType) {
                "Incoming" -> minutesOfDay >= INCOMING_LATE_FROM
                "Outgoing" -> minutesOfDay >= OUTGOING_LATE_FROM
                else -> false
            }
        }
    }
}
