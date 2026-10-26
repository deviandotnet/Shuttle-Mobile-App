package com.example.shuttleapp.domain.usecase.shuttlepass

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.testutil.FakeShuttlePassRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CompleteShuttlePassUseCaseTest {

    private val passengers = listOf(PassengerQR(id = "a", scannedQR = "Passenger_A", timeIn = "06:40:00"))

    private fun at(hourOfDay: Int, minute: Int): () -> Calendar = {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
    }

    @Test
    fun `stamps the arrival and finalizes the pass with its passengers`() = runTest {
        val repository = FakeShuttlePassRepository()
        val useCase = CompleteShuttlePassUseCase(repository, now = at(6, 42))

        val result = useCase(ShuttlePass(id = "p1", driver = "d1", tripType = "Incoming", departure = "06:10"), passengers)

        assertTrue(result is Resource.Success)
        val (finalPass, finalPassengers) = repository.finalized.single()
        assertEquals("06:42", finalPass.arrival)
        assertFalse(finalPass.isLateShuttle)
        assertEquals(passengers, finalPassengers)
    }

    @Test
    fun `incoming trips arriving from 6 55 am are late`() = runTest {
        val repository = FakeShuttlePassRepository()
        CompleteShuttlePassUseCase(repository, now = at(6, 55))(ShuttlePass(tripType = "Incoming"), passengers)
        CompleteShuttlePassUseCase(repository, now = at(8, 5))(ShuttlePass(tripType = "Incoming"), passengers)

        assertTrue(repository.finalized.all { it.first.isLateShuttle })
    }

    @Test
    fun `incoming trips arriving in the afternoon are late`() {
        assertTrue(CompleteShuttlePassUseCase.isLateShuttle("Incoming", at(14, 0)()))
    }

    @Test
    fun `incoming trips arriving before 6 55 am are on time`() {
        assertFalse(CompleteShuttlePassUseCase.isLateShuttle("Incoming", at(6, 54)()))
    }

    @Test
    fun `outgoing trips arriving from 6 55 pm are late`() {
        assertTrue(CompleteShuttlePassUseCase.isLateShuttle("Outgoing", at(18, 55)()))
        assertTrue(CompleteShuttlePassUseCase.isLateShuttle("Outgoing", at(20, 0)()))
        assertFalse(CompleteShuttlePassUseCase.isLateShuttle("Outgoing", at(18, 54)()))
        assertFalse(CompleteShuttlePassUseCase.isLateShuttle("Outgoing", at(8, 0)()))
    }

    @Test
    fun `a failed save is passed back to the caller`() = runTest {
        val repository = FakeShuttlePassRepository().apply { writeResult = Resource.Error("disk full") }

        val result = CompleteShuttlePassUseCase(repository, now = at(7, 0))(ShuttlePass(tripType = "Incoming"), passengers)

        assertEquals("disk full", (result as Resource.Error).message)
    }
}
