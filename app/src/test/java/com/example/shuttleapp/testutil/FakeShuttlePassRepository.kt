package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.model.SyncResult
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeShuttlePassRepository : ShuttlePassRepository {

    val passes: Map<PassFilter, MutableStateFlow<List<ShuttlePassWithPassenger>>> =
        PassFilter.entries.associateWith { MutableStateFlow(emptyList()) }

    var syncResult: Resource<SyncResult> = Resource.Success(SyncResult(syncedCount = 0, message = null))

    /** When set, syncUnsynced suspends until it is completed, so tests can observe the Running state. */
    var syncGate: CompletableDeferred<Unit>? = null
    var syncCalls = 0
        private set

    override fun observePasses(driverId: String, filter: PassFilter): Flow<List<ShuttlePassWithPassenger>> =
        passes.getValue(filter)

    override suspend fun syncUnsynced(driverId: String): Resource<SyncResult> {
        syncCalls++
        syncGate?.await()
        return syncResult
    }

    override fun observePass(id: String): Flow<ShuttlePassWithPassenger?> = emptyFlow()
    /** The pass every observeShuttlePass(id) call sees; null means not found. */
    val shuttlePass = MutableStateFlow<ShuttlePass?>(null)
    override fun observeShuttlePass(id: String): Flow<ShuttlePass?> = shuttlePass
    /** The driver's open draft; null means none. */
    val draft = MutableStateFlow<ShuttlePass?>(null)
    override fun observeDraft(driverId: String): Flow<ShuttlePass?> = draft
    val unsyncedCount = MutableStateFlow(0)

    override fun observeUnsyncedCount(driverId: String): Flow<Int> = unsyncedCount
    override fun observeLateCount(driverId: String): Flow<Int> = flowOf(0)
    override suspend fun getDraftId(driverId: String): String? = null
    val inserted = mutableListOf<ShuttlePass>()
    val updated = mutableListOf<ShuttlePass>()
    val deleted = mutableListOf<String>()
    val finalized = mutableListOf<Pair<ShuttlePass, List<PassengerQR>>>()
    var writeResult: Resource<Unit> = Resource.Success(Unit)
    /** When set, update suspends until it is completed, so tests can act while a write is in flight. */
    var updateGate: CompletableDeferred<Unit>? = null

    override suspend fun insert(pass: ShuttlePass): Resource<Unit> {
        inserted += pass
        return writeResult
    }
    override suspend fun update(passes: List<ShuttlePass>): Resource<Unit> {
        updated += passes
        updateGate?.await()
        return writeResult
    }
    override suspend fun delete(id: String): Resource<Unit> {
        deleted += id
        return writeResult
    }
    override suspend fun finalizeDraft(driverId: String, pass: ShuttlePass, passengers: List<PassengerQR>): Resource<Unit> {
        finalized += pass to passengers
        return writeResult
    }
    override suspend fun syncPass(id: String): Resource<SyncResult> = syncResult
    override suspend fun purgeExpired(driverId: String): Resource<Int> = Resource.Success(0)
}
