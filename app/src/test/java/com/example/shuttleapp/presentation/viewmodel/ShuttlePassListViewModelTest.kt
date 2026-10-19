package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.model.SyncResult
import com.example.shuttleapp.presentation.events.ShuttlePassListAction
import com.example.shuttleapp.presentation.events.ShuttlePassListEvent
import com.example.shuttleapp.domain.model.TokenStatus
import com.example.shuttleapp.presentation.mapper.ShuttlePassItemUiMapper
import com.example.shuttleapp.presentation.state.ReauthState
import com.example.shuttleapp.presentation.state.SyncStatus
import com.example.shuttleapp.testutil.FakeNetworkMonitor
import com.example.shuttleapp.testutil.FakeRoutesRepository
import com.example.shuttleapp.testutil.FakeSessionRepository
import org.junit.Assert.assertNull
import com.example.shuttleapp.testutil.FakeShuttlePassRepository
import com.example.shuttleapp.testutil.MainDispatcherRule
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ShuttlePassListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeShuttlePassRepository
    private lateinit var sessionRepository: FakeSessionRepository
    private lateinit var networkMonitor: FakeNetworkMonitor
    private lateinit var viewModel: ShuttlePassListViewModel

    private fun pass(id: String, created: Long, isLate: Boolean = false) = ShuttlePassWithPassenger(
        shuttlePass = ShuttlePass(
            id = id, routeId = "r1", driver = "driver-1", date = "2025-01-02",
            dateCreated = created, tripType = "Incoming", departure = "06:15:00", arrival = "07:05:00",
            isLateShuttle = isLate, isDraft = false
        ),
        passengersQR = emptyList()
    )

    @Before
    fun setUp() {
        repository = FakeShuttlePassRepository()
        sessionRepository = FakeSessionRepository()
        networkMonitor = FakeNetworkMonitor()
        viewModel = ShuttlePassListViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "driver-1")),
            shuttlePassRepository = repository,
            itemUiMapper = ShuttlePassItemUiMapper(FakeRoutesRepository(mapOf("r1" to "Villa Estrella"))),
            sessionRepository = sessionRepository,
            networkMonitor = networkMonitor
        )
    }

    @Test
    fun `upload while offline is refused without calling the api`() = runTest {
        networkMonitor.isOnline.value = false

        viewModel.state.test {
            assertFalse(expectMostRecentItem().isOnline)
            viewModel.onAction(ShuttlePassListAction.UploadAll)
            assertEquals(SyncStatus.Failed(ShuttlePassListViewModel.OFFLINE_MESSAGE), awaitItem().sync)
        }
        assertEquals(0, repository.syncCalls)
    }

    @Test
    fun `expired token shows sign-in banner and upload opens the sign-in dialog`() = runTest {
        sessionRepository.setTokenStatus(TokenStatus.Expired)

        viewModel.state.test {
            assertTrue(expectMostRecentItem().needsSignIn)
            viewModel.onAction(ShuttlePassListAction.UploadAll)
            assertEquals(ReauthState(username = "jdelacruz"), awaitItem().reauth)
        }
        assertEquals(0, repository.syncCalls)
    }

    @Test
    fun `successful sign-in closes the dialog and clears the banner`() = runTest {
        sessionRepository.setTokenStatus(TokenStatus.Expired)

        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(ShuttlePassListAction.ShowReauth)
            viewModel.onAction(ShuttlePassListAction.SubmitReauth("jdelacruz", "secret"))
            val state = expectMostRecentItem()
            assertNull(state.reauth)
            assertFalse(state.needsSignIn)
        }
        assertEquals(listOf("jdelacruz" to "secret"), sessionRepository.reauthAttempts)
    }

    @Test
    fun `failed sign-in keeps the dialog open with the error`() = runTest {
        sessionRepository.setTokenStatus(TokenStatus.Expired)
        sessionRepository.reauthResult = Resource.Error("Sign in failed. wrong password")

        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(ShuttlePassListAction.ShowReauth)
            viewModel.onAction(ShuttlePassListAction.SubmitReauth("jdelacruz", "nope"))
            val state = expectMostRecentItem()
            assertEquals("Sign in failed. wrong password", state.reauth?.error)
            assertFalse(state.reauth!!.isSubmitting)
            assertTrue(state.needsSignIn)
        }
    }

    @Test
    fun `loads All passes newest first with formatted fields`() = runTest {
        repository.passes.getValue(PassFilter.All).value = listOf(pass("old", 1_000), pass("new", 2_000))

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(listOf("new", "old"), state.items.map { it.id })
            assertEquals("Villa Estrella · Incoming", state.items.first().routeAndTrip)
            assertEquals("2025-01-02  06:15 — 07:05", state.items.first().schedule)
            assertTrue(state.items.first().code.endsWith("-NEW"))
        }
    }

    @Test
    fun `switching filter stops listening to the previous filter`() = runTest {
        repository.passes.getValue(PassFilter.All).value = listOf(pass("a", 1))
        repository.passes.getValue(PassFilter.Late).value = listOf(pass("late", 2, isLate = true))

        viewModel.state.test {
            assertEquals(listOf("a"), awaitItem().items.map { it.id })

            viewModel.onAction(ShuttlePassListAction.SelectFilter(PassFilter.Late))
            val late = expectMostRecentItem()
            assertEquals(PassFilter.Late, late.filter)
            assertEquals(listOf("late"), late.items.map { it.id })

            // A change in the All query must not overwrite the Late list.
            repository.passes.getValue(PassFilter.All).value = listOf(pass("b", 3))
            expectNoEvents()
        }
    }

    @Test
    fun `upload all goes running then success`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.syncGate = gate
        repository.syncResult = Resource.Success(SyncResult(syncedCount = 2, message = null))

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ShuttlePassListAction.UploadAll)
            assertEquals(SyncStatus.Running, awaitItem().sync)

            viewModel.onAction(ShuttlePassListAction.UploadAll) // ignored while running
            gate.complete(Unit)
            assertEquals(SyncStatus.Success("Uploaded 2 shuttle pass(es)."), awaitItem().sync)
        }
        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `upload failure shows the error and dismiss returns to idle`() = runTest {
        repository.syncResult = Resource.Error("No unsynced shuttle pass")

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(ShuttlePassListAction.UploadAll)
            assertEquals(SyncStatus.Failed("No unsynced shuttle pass"), expectMostRecentItem().sync)

            viewModel.onAction(ShuttlePassListAction.DismissSyncResult)
            assertEquals(SyncStatus.Idle, awaitItem().sync)
        }
    }

    @Test
    fun `open pass emits a navigation event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(ShuttlePassListAction.OpenPass("p1"))
            assertEquals(ShuttlePassListEvent.NavigateToPass("p1"), awaitItem())
        }
    }
}
