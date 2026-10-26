package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.Route
import com.example.shuttleapp.domain.model.Shuttle
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.model.ShuttleProvider
import com.example.shuttleapp.domain.model.UserData
import com.example.shuttleapp.domain.usecase.manualaddpassenger.ValidateQRCodeInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidatePlateNumberInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateRouteInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateTripTypeInput
import com.example.shuttleapp.domain.usecase.shuttlepass.CompleteShuttlePassUseCase
import com.example.shuttleapp.domain.usecase.shuttlepass.CreateDraftPassUseCase
import com.example.shuttleapp.presentation.events.CurrentPassAction
import com.example.shuttleapp.presentation.events.CurrentPassEvent
import com.example.shuttleapp.presentation.state.CurrentPassMode
import com.example.shuttleapp.presentation.state.PassActionUi
import com.example.shuttleapp.testutil.FakePassengerQRRepository
import com.example.shuttleapp.testutil.FakeRoutesRepository
import com.example.shuttleapp.testutil.FakeShuttlePassRepository
import com.example.shuttleapp.testutil.FakeShuttleProviderRepository
import com.example.shuttleapp.testutil.FakeShuttleRepository
import com.example.shuttleapp.testutil.FakeUserDataRepository
import com.example.shuttleapp.testutil.MainDispatcherRule
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrentPassViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val driver = UserData("d1", "15125112", "Christian", "Losamia", "", providerId = "prov1")
    private val draft = ShuttlePass(id = "p1", routeId = "r1", provider = "prov1", driver = "d1", plateNumber = "s1", tripType = "Incoming", date = "2026-10-20")

    private lateinit var passes: FakeShuttlePassRepository
    private lateinit var passengers: FakePassengerQRRepository
    private lateinit var viewModel: CurrentPassViewModel

    @Before
    fun setUp() {
        passes = FakeShuttlePassRepository()
        passengers = FakePassengerQRRepository()
        viewModel = createViewModel(driver)
    }

    private fun createViewModel(user: UserData?): CurrentPassViewModel {
        val routes = FakeRoutesRepository(names = mapOf("r1" to "Villa Estrella"), routes = listOf(Route("VE", "r1", "Villa Estrella")))
        val shuttles = FakeShuttleRepository(listOf(Shuttle("s1", "ABDA 3332", "prov1")))
        return CurrentPassViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "d1")),
            shuttlePassRepository = passes,
            passengerQRRepository = passengers,
            routesRepository = routes,
            shuttleRepository = shuttles,
            userDataRepository = FakeUserDataRepository(user),
            shuttleProviderRepository = FakeShuttleProviderRepository(listOf(ShuttleProvider("prov1", "BCTSS"))),
            validateQRCodeInput = ValidateQRCodeInput(),
            createDraftPass = CreateDraftPassUseCase(passes, routes, shuttles, ValidateRouteInput(), ValidateTripTypeInput(), ValidatePlateNumberInput()),
            completeShuttlePass = CompleteShuttlePassUseCase(passes)
        )
    }

    @Test
    fun `creating is blocked with a message until the driver's provider is known`() = runTest {
        viewModel = createViewModel(driver.copy(providerId = ""))
        subscribe()

        viewModel.events.test {
            viewModel.onAction(CurrentPassAction.CreatePass)

            assertTrue(awaitItem() is CurrentPassEvent.ShowMessage)
            assertTrue(passes.inserted.isEmpty())
        }
    }

    @Test
    fun `repeat departure taps while the first is saving record it once`() = runTest {
        passes.draft.value = draft
        passes.updateGate = CompletableDeferred()
        subscribe()

        viewModel.onAction(CurrentPassAction.StartDeparture)
        viewModel.onAction(CurrentPassAction.StartDeparture)
        passes.updateGate?.complete(Unit)

        assertEquals(1, passes.updated.size)
    }

    /** state uses WhileSubscribed, so keep a subscriber for the whole test. */
    private fun TestScope.subscribe() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect() }
    }

    @Test
    fun `without a draft the create form is shown with routes and the provider's shuttles`() = runTest {
        subscribe()

        val state = viewModel.state.value
        assertEquals(CurrentPassMode.Create, state.mode)
        assertEquals(listOf("Villa Estrella"), state.form.routes)
        assertEquals(listOf("ABDA 3332"), state.form.plateNumbers)
    }

    @Test
    fun `creating with missing fields shows each field's error and saves nothing`() = runTest {
        subscribe()

        viewModel.onAction(CurrentPassAction.CreatePass)

        val form = viewModel.state.value.form
        assertEquals("Route can't be blank", form.routeError)
        assertEquals("Trip type can't be blank", form.tripTypeError)
        assertEquals("Plate number can't be blank", form.plateNumberError)
        assertTrue(passes.inserted.isEmpty())
    }

    @Test
    fun `creating a valid pass saves a draft with resolved ids`() = runTest {
        subscribe()

        viewModel.onAction(CurrentPassAction.RouteChanged("Villa Estrella"))
        viewModel.onAction(CurrentPassAction.TripTypeSelected("Incoming"))
        viewModel.onAction(CurrentPassAction.PlateNumberChanged("ABDA 3332"))
        viewModel.onAction(CurrentPassAction.CreatePass)

        val saved = passes.inserted.single()
        assertEquals("r1", saved.routeId)
        assertEquals("s1", saved.plateNumber)
        assertEquals("d1", saved.driver)
        assertEquals("prov1", saved.provider)
        assertTrue(saved.isDraft)
    }

    @Test
    fun `an open draft shows its card, manifest and next action`() = runTest {
        passes.draft.value = draft
        passengers.passengers.value = listOf(PassengerQR(id = "a", shuttlePassId = "p1", scannedQR = "Passenger_A", timeIn = "16:20:00"))
        subscribe()

        val mode = viewModel.state.value.mode as CurrentPassMode.Active
        assertEquals("Villa Estrella", mode.card.fromLabel)
        assertEquals("ABDA 3332", mode.card.plateNumber)
        assertEquals("BCTSS", mode.card.provider)
        assertEquals("Christian Losamia", mode.card.driver)
        assertEquals(1, mode.passengers.size)
        assertEquals(PassActionUi.StartDeparture, mode.action)
    }

    @Test
    fun `starting departure records the time`() = runTest {
        passes.draft.value = draft
        subscribe()

        viewModel.events.test {
            viewModel.onAction(CurrentPassAction.StartDeparture)

            assertTrue(passes.updated.single().departure.isNotBlank())
            assertTrue(awaitItem() is CurrentPassEvent.ShowMessage)
        }
    }

    @Test
    fun `completing finalizes the draft with its passengers and leaves the screen`() = runTest {
        val boarded = PassengerQR(id = "a", shuttlePassId = "p1", scannedQR = "Passenger_A", timeIn = "16:20:00")
        passes.draft.value = draft.copy(departure = "16:30")
        passengers.passengers.value = listOf(boarded)
        subscribe()

        viewModel.events.test {
            viewModel.onAction(CurrentPassAction.CompletePass)

            assertEquals(listOf(boarded), passes.finalized.single().second)
            assertEquals(CurrentPassEvent.NavigateBack("Shuttle pass completed"), awaitItem())
        }
    }

    @Test
    fun `completing is ignored until a passenger has boarded`() = runTest {
        passes.draft.value = draft.copy(departure = "16:30")
        subscribe()

        viewModel.onAction(CurrentPassAction.CompletePass)

        assertTrue(passes.finalized.isEmpty())
    }

    @Test
    fun `a failed completion keeps the driver on the screen with a message`() = runTest {
        passes.draft.value = draft.copy(departure = "16:30")
        passengers.passengers.value = listOf(PassengerQR(id = "a", scannedQR = "Passenger_A", timeIn = "16:20:00"))
        passes.writeResult = Resource.Error("disk full")
        subscribe()

        viewModel.events.test {
            viewModel.onAction(CurrentPassAction.CompletePass)
            assertEquals(CurrentPassEvent.ShowMessage("disk full"), awaitItem())
        }
    }

    @Test
    fun `deleting the draft asks first, then removes it and leaves`() = runTest {
        passes.draft.value = draft
        subscribe()

        viewModel.onAction(CurrentPassAction.RequestDeletePass)
        assertTrue(viewModel.state.value.showDeleteConfirm)

        viewModel.events.test {
            viewModel.onAction(CurrentPassAction.ConfirmDeletePass)

            assertEquals(listOf("p1"), passes.deleted)
            assertEquals(CurrentPassEvent.NavigateBack("Draft deleted"), awaitItem())
        }
    }

    @Test
    fun `editing a passenger rejects a code already on board and saves a valid one`() = runTest {
        passes.draft.value = draft
        passengers.passengers.value = listOf(
            PassengerQR(id = "a", scannedQR = "Passenger_A", timeIn = "16:20:00"),
            PassengerQR(id = "b", scannedQR = "Passenger_B", timeIn = "16:21:00")
        )
        subscribe()

        viewModel.onAction(CurrentPassAction.EditPassenger("b"))
        viewModel.onAction(CurrentPassAction.EditedQrChanged("Passenger_A"))
        viewModel.onAction(CurrentPassAction.SaveEditedPassenger)
        assertEquals("Passenger was already onboarded", viewModel.state.value.editingPassenger?.error)

        viewModel.onAction(CurrentPassAction.EditedQrChanged("Passenger_C"))
        viewModel.onAction(CurrentPassAction.SaveEditedPassenger)

        assertNull(viewModel.state.value.editingPassenger)
        assertEquals("Passenger_C", passengers.passengers.value.first { it.id == "b" }.scannedQR)
    }
}
