package com.example.shuttleapp.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * When each driver's passes last uploaded successfully from this device. Kept on the device only:
 * the backend has no such field, and the drawer and Sync tab just need "last synced today, 10:42".
 */
interface SyncHistoryRepository {
    /** Epoch millis of the last successful upload, or null if this device never uploaded for [driverId]. */
    fun observeLastSyncedAt(driverId: String): Flow<Long?>

    suspend fun recordSync(driverId: String, atMillis: Long)
}
