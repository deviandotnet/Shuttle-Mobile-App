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
    override fun observeShuttlePass(id: String): Flow<ShuttlePass?> = emptyFlow()
    override fun observeDraft(driverId: String): Flow<ShuttlePass?> = emptyFlow()
    override fun observeUnsyncedCount(driverId: String): Flow<Int> = flowOf(0)
    override fun observeLateCount(driverId: String): Flow<Int> = flowOf(0)
    override suspend fun getDraftId(driverId: String): String? = null
    override suspend fun insert(pass: ShuttlePass): Resource<Unit> = Resource.Success(Unit)
    override suspend fun update(passes: List<ShuttlePass>): Resource<Unit> = Resource.Success(Unit)
    override suspend fun delete(id: String): Resource<Unit> = Resource.Success(Unit)
    override suspend fun finalizeDraft(driverId: String, pass: ShuttlePass, passengers: List<PassengerQR>): Resource<Unit> =
        Resource.Success(Unit)
    override suspend fun syncPass(id: String): Resource<SyncResult> = syncResult
    override suspend fun purgeExpired(driverId: String): Resource<Int> = Resource.Success(0)
}
