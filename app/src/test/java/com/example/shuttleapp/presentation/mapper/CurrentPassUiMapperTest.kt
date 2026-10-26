package com.example.shuttleapp.presentation.mapper

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.presentation.state.CreatePassFormUi
import com.example.shuttleapp.presentation.state.PassActionUi
import com.example.shuttleapp.presentation.state.PassStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentPassUiMapperTest {

    private val names = PassNames(route = "Villa Estrella", plateNumber = "ABDA 3332", provider = "BCTSS", driver = "Christian Losamia")
    private val draft = ShuttlePass(id = "p1", tripType = "Incoming", date = "2026-10-20", isDraft = true)

    @Test
    fun `incoming trip runs from the route to the drop-off point`() {
        val card = CurrentPassUiMapper.toCard(draft, names)
        assertEquals("Villa Estrella", card.fromLabel)
        assertFalse(card.fromIsGeneric)
        assertEquals(CurrentPassUiMapper.DROP_OFF_POINT, card.toLabel)
        assertTrue(card.toIsGeneric)
    }

    @Test
    fun `outgoing trip runs from the pick-up point to the route`() {
        val card = CurrentPassUiMapper.toCard(draft.copy(tripType = "Outgoing"), names)
        assertEquals(CurrentPassUiMapper.PICK_UP_POINT, card.fromLabel)
        assertTrue(card.fromIsGeneric)
        assertEquals("Villa Estrella", card.toLabel)
    }

    @Test
    fun `unset times show a placeholder and recorded times drop the seconds`() {
        val notDeparted = CurrentPassUiMapper.toCard(draft, names)
        assertEquals(CurrentPassUiMapper.TIME_UNSET, notDeparted.departure)
        assertFalse(notDeparted.hasDeparture)

        val departed = CurrentPassUiMapper.toCard(draft.copy(departure = "16:30:12"), names)
        assertEquals("16:30", departed.departure)
        assertTrue(departed.hasDeparture)
        assertEquals(CurrentPassUiMapper.TIME_UNSET, departed.arrival)
    }

    @Test
    fun `status follows the pass lifecycle`() {
        assertEquals(PassStatus.NotDeparted, CurrentPassUiMapper.toCard(draft, names).status)
        assertEquals(PassStatus.InTransit, CurrentPassUiMapper.toCard(draft.copy(departure = "16:30"), names).status)
        assertEquals(PassStatus.Completed, CurrentPassUiMapper.toCard(draft.copy(departure = "16:30", isDraft = false), names).status)
    }

    @Test
    fun `rows are numbered in boarding order and listed newest first`() {
        val rows = CurrentPassUiMapper.toRows(
            listOf(
                PassengerQR(id = "a", scannedQR = "Passenger_A", timeIn = "16:15:40"),
                PassengerQR(id = "b", scannedQR = "Passenger_B", timeIn = "16:22:05"),
                PassengerQR(id = "c", scannedQR = "Passenger_C", timeIn = "16:24:00")
            )
        )
        assertEquals(listOf("c", "b", "a"), rows.map { it.id })
        assertEquals(listOf(3, 2, 1), rows.map { it.number })
        assertEquals("16:24", rows.first().time)
    }

    @Test
    fun `rows keep boarding order across midnight`() {
        val rows = CurrentPassUiMapper.toRows(
            listOf(
                PassengerQR(id = "late", scannedQR = "Passenger_L", timeIn = "23:58"),
                PassengerQR(id = "early", scannedQR = "Passenger_E", timeIn = "00:02")
            )
        )
        assertEquals(listOf("early", "late"), rows.map { it.id })
        assertEquals(listOf(2, 1), rows.map { it.number })
    }

    @Test
    fun `actions move from start departure to a swipe that needs passengers`() {
        assertEquals(PassActionUi.StartDeparture, CurrentPassUiMapper.toAction(draft, passengerCount = 3))

        val departed = draft.copy(departure = "16:30")
        val blocked = CurrentPassUiMapper.toAction(departed, passengerCount = 0) as PassActionUi.Complete
        assertFalse(blocked.enabled)
        assertEquals(CurrentPassUiMapper.COMPLETE_HINT, blocked.hint)

        val ready = CurrentPassUiMapper.toAction(departed, passengerCount = 2) as PassActionUi.Complete
        assertTrue(ready.enabled)

        assertEquals(PassActionUi.None, CurrentPassUiMapper.toAction(departed.copy(isDraft = false), passengerCount = 2))
    }

    @Test
    fun `form preview reflects what has been picked`() {
        val empty = CurrentPassUiMapper.withPreview(CreatePassFormUi())
        assertEquals("Route → Drop-off point", empty.previewTitle)
        assertEquals("Trip type not set · Shuttle not selected", empty.previewSubtitle)

        val filled = CurrentPassUiMapper.withPreview(CreatePassFormUi(route = "Mamatid", tripType = "Outgoing", plateNumber = "LMN-222"))
        assertEquals("Pick-up point → Mamatid", filled.previewTitle)
        assertEquals("Outgoing · LMN-222", filled.previewSubtitle)
    }
}
