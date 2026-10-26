package com.example.shuttleapp.presentation.mapper

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.presentation.state.CreatePassFormUi
import com.example.shuttleapp.presentation.state.PassActionUi
import com.example.shuttleapp.presentation.state.PassCardUi
import com.example.shuttleapp.presentation.state.PassStatus
import com.example.shuttleapp.presentation.state.PassengerRowUi
import com.example.shuttleapp.presentation.state.TRIP_INCOMING
import com.example.shuttleapp.presentation.state.TRIP_OUTGOING
import com.example.shuttleapp.util.formatPassDate
import com.example.shuttleapp.util.formatTimeHHmm

/** Display names looked up from the ids a pass stores. */
data class PassNames(
    val route: String = "",
    val plateNumber: String = "",
    val provider: String = "",
    val driver: String = ""
)

/** Pure formatting for the current-pass and pass-detail screens. */
object CurrentPassUiMapper {

    const val PICK_UP_POINT = "Pick-up point"
    const val DROP_OFF_POINT = "Drop-off point"
    const val TIME_UNSET = "--:--"
    const val COMPLETE_HINT = "Add at least one passenger to complete"

    fun toCard(pass: ShuttlePass, names: PassNames): PassCardUi {
        // Incoming trips run from the route to the plant; outgoing trips the other way.
        val outgoing = pass.tripType.equals(TRIP_OUTGOING, ignoreCase = true)
        return PassCardUi(
            status = when {
                !pass.isDraft -> PassStatus.Completed
                pass.departure.isBlank() -> PassStatus.NotDeparted
                else -> PassStatus.InTransit
            },
            dateLabel = formatPassDate(pass.date),
            fromLabel = if (outgoing) PICK_UP_POINT else names.route,
            fromIsGeneric = outgoing,
            toLabel = if (outgoing) names.route else DROP_OFF_POINT,
            toIsGeneric = !outgoing,
            departure = time(pass.departure),
            hasDeparture = pass.departure.isNotBlank(),
            arrival = time(pass.arrival),
            hasArrival = pass.arrival.isNotBlank(),
            plateNumber = names.plateNumber,
            provider = names.provider,
            driver = names.driver
        )
    }

    /**
     * [passengers] must already be in boarding order (Room returns them in insert order). Numbered from
     * 1 = first aboard, newest shown first. Not sorted by timeIn: "HH:mm" text misorders trips past midnight.
     */
    fun toRows(passengers: List<PassengerQR>): List<PassengerRowUi> =
        passengers
            .mapIndexed { index, p -> PassengerRowUi(p.id, index + 1, p.scannedQR, formatTimeHHmm(p.timeIn)) }
            .reversed()

    fun toAction(pass: ShuttlePass, passengerCount: Int): PassActionUi = when {
        !pass.isDraft -> PassActionUi.None
        pass.departure.isBlank() -> PassActionUi.StartDeparture
        passengerCount == 0 -> PassActionUi.Complete(enabled = false, hint = COMPLETE_HINT)
        else -> PassActionUi.Complete(enabled = true, hint = null)
    }

    /** Fills the form's derived fields (trip hint and the live preview) from what has been picked. */
    fun withPreview(form: CreatePassFormUi): CreatePassFormUi {
        val outgoing = form.tripType.equals(TRIP_OUTGOING, ignoreCase = true)
        val route = form.route.ifBlank { "Route" }
        val title = if (outgoing) "$PICK_UP_POINT → $route" else "$route → $DROP_OFF_POINT"
        val trip = form.tripType.ifBlank { "Trip type not set" }
        val shuttle = form.plateNumber.ifBlank { "Shuttle not selected" }
        return form.copy(
            tripTypeHint = when {
                outgoing -> "Outgoing picks up at the plant and drops off along the route."
                form.tripType.equals(TRIP_INCOMING, ignoreCase = true) ->
                    "Incoming picks up along the route and drops off at the plant."
                else -> null
            },
            previewTitle = title,
            previewSubtitle = "$trip · $shuttle"
        )
    }

    private fun time(value: String) = if (value.isBlank()) TIME_UNSET else formatTimeHHmm(value)
}
