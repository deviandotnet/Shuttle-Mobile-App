package com.example.shuttleapp.domain.usecase.shuttlepass

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import com.example.shuttleapp.testutil.FakePassengerQRRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardPassengerUseCaseTest {

    private val repository = FakePassengerQRRepository()
    private val boardPassenger = BoardPassengerUseCase(repository, now = { "16:26" })

    /** In boarding order, as Room returns them. */
    private val onBoard = listOf(
        PassengerQR(id = "a", shuttlePassId = "p1", scannedQR = "Passenger_A", timeIn = "16:19"),
        PassengerQR(id = "b", shuttlePassId = "p1", scannedQR = "Passenger_B", timeIn = "16:22")
    )

    @Test
    fun `boarding numbers follow boarding order across midnight`() = runTest {
        val pastMidnight = listOf(
            PassengerQR(id = "x", shuttlePassId = "p1", scannedQR = "Passenger_X", timeIn = "23:58"),
            PassengerQR(id = "y", shuttlePassId = "p1", scannedQR = "Passenger_Y", timeIn = "00:02")
        )

        val result = boardPassenger("p1", "Passenger_X", pastMidnight) as BoardingResult.AlreadyOnBoard

        assertEquals(1, result.number)
        assertEquals("23:58", result.passenger.timeIn)
    }

    @Test
    fun `a QR without the passenger marker is rejected and nothing is saved`() = runTest {
        val result = boardPassenger("p1", "https://example.com/menu", onBoard)

        assertEquals(BoardingResult.NotPassenger, result)
        assertTrue(repository.passengers.value.isEmpty())
    }

    @Test
    fun `a passenger already aboard is reported with their boarding number and time`() = runTest {
        val result = boardPassenger("p1", "Passenger_A", onBoard)

        val duplicate = result as BoardingResult.AlreadyOnBoard
        assertEquals(1, duplicate.number)
        assertEquals("16:19", duplicate.passenger.timeIn)
        assertTrue(repository.passengers.value.isEmpty())
    }

    @Test
    fun `a new passenger is saved as a draft with the next boarding number`() = runTest {
        val result = boardPassenger("p1", "passenger_C", onBoard)

        val boarded = result as BoardingResult.Boarded
        assertEquals(3, boarded.number)
        val saved = repository.passengers.value.single()
        assertEquals("p1", saved.shuttlePassId)
        assertEquals("passenger_C", saved.scannedQR)
        assertEquals("16:26", saved.timeIn)
        assertTrue(saved.isDraft)
    }

    @Test
    fun `a failed save is reported instead of thrown`() = runTest {
        val failing = BoardPassengerUseCase(ThrowingRepository(), now = { "16:26" })

        val result = failing("p1", "Passenger_C", onBoard)

        assertTrue(result is BoardingResult.Failed)
    }

    private class ThrowingRepository : PassengerQRRepository {
        override suspend fun getPassengerId(name: String, shuttlePassId: String): String? = null
        override fun getDraftedPassengers(shuttlePassId: String): Flow<List<PassengerQR>> = emptyFlow()
        override fun getPassengerById(id: String): Flow<PassengerQR> = emptyFlow()
        override suspend fun deletePassenger(passenger: PassengerQR) = Unit
        override suspend fun updatePassenger(passenger: PassengerQR) = Unit
        override suspend fun insertPassenger(passenger: PassengerQR) = throw IllegalStateException("disk full")
    }
}
