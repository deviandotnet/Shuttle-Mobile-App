package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.repository.SyncHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSyncHistoryRepository : SyncHistoryRepository {
    val lastSynced = MutableStateFlow<Map<String, Long>>(emptyMap())

    override fun observeLastSyncedAt(driverId: String): Flow<Long?> = lastSynced.map { it[driverId] }

    override suspend fun recordSync(driverId: String, atMillis: Long) {
        lastSynced.value = lastSynced.value + (driverId to atMillis)
    }
}
