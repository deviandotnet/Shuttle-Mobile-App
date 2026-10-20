package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.model.SyncResult
import com.example.shuttleapp.domain.model.TokenStatus
import com.example.shuttleapp.presentation.events.SyncAction
import com.example.shuttleapp.presentation.events.SyncEvent
import com.example.shuttleapp.presentation.mapper.ShuttlePassItemUiMapper
import com.example.shuttleapp.presentation.state.ReauthState
import com.example.shuttleapp.presentation.state.SyncStatus
import com.example.shuttleapp.testutil.FakeNetworkMonitor
import com.example.shuttleapp.testutil.FakeRoutesRepository
import com.example.shuttleapp.testutil.FakeSessionRepository
import com.example.shuttleapp.testutil.FakeShuttlePassRepository
import com.example.shuttleapp.testutil.FakeSyncHistoryRepository
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

class SyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeShuttlePassRepository
    private lateinit var history: FakeSyncHistoryRepository
    private lateinit var sessionRepository: FakeSessionRepository
    private lateinit var networkMonitor: FakeNetworkMonitor
    private lateinit var viewModel: SyncViewModel

    private fun pass(id: String, created: Long) = ShuttlePassWithPassenger(
        shuttlePass = ShuttlePass(
            id = id, routeId = "r1", driver = "driver-1", date = "2026-10-20",
            dateCreated = created, tripType = "Incoming", departure = "07:45:00", arrival = "08:30:00",
            isDraft = false, isSynced = false
        ),
        passengersQR = emptyList()
    )

    @Before
    fun setUp() {
        repository = FakeShuttlePassRepository()
        history = FakeSyncHistoryRepository()
        sessionRepository = FakeSessionRepository()
        networkMonitor = FakeNetworkMonitor()
        viewModel = SyncViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "driver-1")),
            shuttlePassRepository = repository,
            syncHistoryRepository = history,
            itemUiMapper = ShuttlePassItemUiMapper(FakeRoutesRepository(mapOf("r1" to "Villa Estrella"))),
            uploadControllerFactory = PassUploadController.Factory(repository, sessionRepository, networkMonitor)
        )
    }

    @Test
    fun `lists passes waiting to upload, newest first`() = runTest {
        repository.passes.getValue(PassFilter.NotSynced).value = listOf(pass("old", 1_000), pass("new", 2_000))

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(listOf("new", "old"), state.pending.map { it.id })
            assertEquals(2, state.pendingCount)
        }
    }

    @Test
    fun `last synced reads not-yet until this device records a sync`() = runTest {
        viewModel.state.test {
            assertEquals("Not synced from this device yet", expectMostRecentItem().lastSynced)

            history.recordSync("driver-1", System.currentTimeMillis())
            assertTrue(awaitItem().lastSynced.startsWith("Last synced today, "))
        }
    }

    @Test
    fun `last synced ignores other drivers on the same device`() = runTest {
        history.recordSync("someone-else", System.currentTimeMillis())

        viewModel.state.test {
            assertEquals("Not synced from this device yet", expectMostRecentItem().lastSynced)
        }
    }

    @Test
    fun `sync now while offline is refused without calling the api`() = runTest {
        networkMonitor.isOnline.value = false

        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(SyncAction.SyncNow)
            assertEquals(SyncStatus.Failed(PassUploadController.OFFLINE_MESSAGE), awaitItem().upload.sync)
        }
        assertEquals(0, repository.syncCalls)
    }

    @Test
    fun `sync now is syncing until the upload finishes, and taps meanwhile are ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.syncGate = gate
        repository.syncResult = Resource.Success(SyncResult(syncedCount = 3, message = null))

        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(SyncAction.SyncNow)
            assertTrue(awaitItem().isSyncing)

            viewModel.onAction(SyncAction.SyncNow)
            gate.complete(Unit)
            val done = awaitItem()
            assertFalse(done.isSyncing)
            assertEquals(SyncStatus.Success("Uploaded 3 shuttle pass(es)."), done.upload.sync)

            viewModel.onAction(SyncAction.DismissSyncResult)
            assertEquals(SyncStatus.Idle, awaitItem().upload.sync)
        }
        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `expired token asks to sign in again instead of uploading`() = runTest {
        sessionRepository.setTokenStatus(TokenStatus.Expired)

        viewModel.state.test {
            assertTrue(expectMostRecentItem().upload.needsSignIn)
            viewModel.onAction(SyncAction.SyncNow)
            assertEquals(ReauthState(username = "jdelacruz"), awaitItem().upload.reauth)
        }
        assertEquals(0, repository.syncCalls)
    }

    @Test
    fun `open pass emits a navigation event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(SyncAction.OpenPass("p1"))
            assertEquals(SyncEvent.NavigateToPass("p1"), awaitItem())
        }
    }
}
