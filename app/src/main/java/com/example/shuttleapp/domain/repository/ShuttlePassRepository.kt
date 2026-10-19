package com.example.shuttleapp.domain.repository

import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.model.SyncResult
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface ShuttlePassRepository {

    fun observePasses(driverId: String, filter: PassFilter): Flow<List<ShuttlePassWithPassenger>>

    fun observePass(id: String): Flow<ShuttlePassWithPassenger?>

    fun observeShuttlePass(id: String): Flow<ShuttlePass?>

    fun observeDraft(driverId: String): Flow<ShuttlePass?>

    fun observeUnsyncedCount(driverId: String): Flow<Int>

    fun observeLateCount(driverId: String): Flow<Int>

    suspend fun getDraftId(driverId: String): String?

    suspend fun insert(pass: ShuttlePass): Resource<Unit>

    suspend fun update(passes: List<ShuttlePass>): Resource<Unit>

    suspend fun delete(id: String): Resource<Unit>

    suspend fun finalizeDraft(driverId: String, pass: ShuttlePass, passengers: List<PassengerQR>): Resource<Unit>

    /** Uploads every finalized, unsynced pass of [driverId] and marks exactly the uploaded ones as synced. */
    suspend fun syncUnsynced(driverId: String): Resource<SyncResult>

    suspend fun syncPass(id: String): Resource<SyncResult>

    /** Deletes synced passes and stale drafts older than one month. Returns the number of deleted passes. */
    suspend fun purgeExpired(driverId: String): Resource<Int>
}
