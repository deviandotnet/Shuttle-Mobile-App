package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.usecase.shuttlepass.BoardPassengerUseCase
import com.example.shuttleapp.presentation.events.QrScannerAction
import com.example.shuttleapp.presentation.events.QrScannerEvent
import com.example.shuttleapp.presentation.events.ScanFeedback
import com.example.shuttleapp.presentation.state.CameraAccess
import com.example.shuttleapp.presentation.state.ScanResultUi
import com.example.shuttleapp.testutil.FakePassengerQRRepository
import com.example.shuttleapp.testutil.FakeRoutesRepository
import com.example.shuttleapp.testutil.FakeShuttlePassRepository
import com.example.shuttleapp.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QrScannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pass = ShuttlePass(id = "p1", routeId = "r1", tripType = "Incoming", departure = "16:30", date = "2026-10-20", isDraft = true)

    private lateinit var passes: FakeShuttlePassRepository
    private lateinit var passengers: FakePassengerQRRepository
    private lateinit var viewModel: QrScannerViewModel

    @Before
    fun setUp() {
        passes = FakeShuttlePassRepository().apply { shuttlePass.value = pass }
        passengers = FakePassengerQRRepository()
        viewModel = QrScannerViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "p1")),
            shuttlePassRepository = passes,
            passengerQRRepository = passengers,
            routesRepository = FakeRoutesRepository(names = mapOf("r1" to "Villa Estrella")),
            boardPassenger = BoardPassengerUseCase(passengers, now = { "16:26" })
        )
    }

    /** state uses WhileSubscribed, so keep a subscriber for the whole test. */
    private fun TestScope.subscribe() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect() }
    }

    private fun passenger(n: Int) = PassengerQR(id = "$n", shuttlePassId = "p1", scannedQR = "Passenger_$n", timeIn = "16:1$n")

    @Test
    fun `state shows the trip, the headcount and the three newest passengers`() = runTest {
        passengers.passengers.value = (1..4).map(::passenger)
        subscribe()

        val state = viewModel.state.value
        assertEquals("Villa Estrella → Drop-off point", state.trip.routeLabel)
        assertEquals("Departed 16:30", state.trip.timeLabel)
        assertEquals(4, state.onBoardCount)
        assertEquals(listOf(4, 3, 2), state.recentRows.map { it.number })
    }

    @Test
    fun `scanning a new passenger boards them, highlights the row and confirms`() = runTest {
        passengers.passengers.value = listOf(passenger(1))
        subscribe()

        viewModel.events.test {
            viewModel.onAction(QrScannerAction.QrScanned("Passenger_9"))

            assertEquals(QrScannerEvent.Feedback(ScanFeedback.Success), awaitItem())
            val result = viewModel.state.value.result as ScanResultUi.Boarded
            assertEquals(2, result.number)
            assertEquals(result.passengerId, viewModel.state.value.highlightedRowId)
            assertEquals(2, viewModel.state.value.onBoardCount)
        }
    }

    @Test
    fun `a code held in view is boarded once, then reported as already aboard after the cooldown`() = runTest {
        subscribe()

        viewModel.onAction(QrScannerAction.QrScanned("Passenger_9"))
        viewModel.onAction(QrScannerAction.QrScanned("Passenger_9"))
        assertEquals(1, passengers.passengers.value.size)
        assertTrue(viewModel.state.value.result is ScanResultUi.Boarded)

        advanceTimeBy(QrScannerViewModel.REPEAT_COOLDOWN_MS + 1)
        viewModel.onAction(QrScannerAction.QrScanned("Passenger_9"))

        assertEquals(1, passengers.passengers.value.size)
        val duplicate = viewModel.state.value.result as ScanResultUi.AlreadyOnBoard
        assertEquals(1, duplicate.number)
    }

    @Test
    fun `a non-passenger QR is rejected with error feedback and saves nothing`() = runTest {
        subscribe()

        viewModel.events.test {
            viewModel.onAction(QrScannerAction.QrScanned("WIFI:S:Lobby;;"))

            assertEquals(QrScannerEvent.Feedback(ScanFeedback.Error), awaitItem())
            assertEquals(ScanResultUi.NotPassenger, viewModel.state.value.result)
            assertTrue(passengers.passengers.value.isEmpty())
        }
    }

    @Test
    fun `the result card clears itself`() = runTest {
        subscribe()

        viewModel.onAction(QrScannerAction.QrScanned("Passenger_9"))
        advanceTimeBy(QrScannerViewModel.RESULT_VISIBLE_MS + 1)

        assertNull(viewModel.state.value.result)
        assertNull(viewModel.state.value.highlightedRowId)
    }

    @Test
    fun `the torch only turns on once the camera is allowed`() = runTest {
        subscribe()

        viewModel.onAction(QrScannerAction.ToggleTorch)
        assertFalse(viewModel.state.value.torchOn)

        viewModel.onAction(QrScannerAction.CameraPermissionResult(granted = true))
        viewModel.onAction(QrScannerAction.ToggleTorch)
        assertEquals(CameraAccess.Granted, viewModel.state.value.camera)
        assertTrue(viewModel.state.value.torchOn)

        viewModel.onAction(QrScannerAction.CameraPermissionResult(granted = false))
        assertFalse(viewModel.state.value.torchOn)
        assertFalse(viewModel.state.value.torchEnabled)
    }

    @Test
    fun `done goes back`() = runTest {
        viewModel.events.test {
            viewModel.onAction(QrScannerAction.Done)
            assertEquals(QrScannerEvent.NavigateBack, awaitItem())
        }
    }
}
