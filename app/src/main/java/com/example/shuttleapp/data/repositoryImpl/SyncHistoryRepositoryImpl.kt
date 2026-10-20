package com.example.shuttleapp.data.repositoryImpl

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.shuttleapp.domain.repository.SyncHistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Stores last-sync times in plain SharedPreferences: a timestamp per account id, not sensitive data. */
@Singleton
class SyncHistoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncHistoryRepository {

    private val prefs: SharedPreferences by lazy { context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE) }

    /** Driver id to last sync time, loaded on first read of each driver. */
    private val cache = MutableStateFlow<Map<String, Long?>>(emptyMap())

    override fun observeLastSyncedAt(driverId: String): Flow<Long?> {
        if (driverId !in cache.value) {
            val stored = read(driverId)
            cache.update { if (driverId in it) it else it + (driverId to stored) }
        }
        return cache.map { it[driverId] }.distinctUntilChanged()
    }

    override suspend fun recordSync(driverId: String, atMillis: Long) {
        cache.update { it + (driverId to atMillis) }
        withContext(Dispatchers.IO) {
            try {
                prefs.edit { putLong(key(driverId), atMillis) }
            } catch (e: Exception) {
                // The time still shows for this run; it just won't survive a restart.
                Log.e(TAG, "Could not save last sync time", e)
            }
        }
    }

    private fun read(driverId: String): Long? = try {
        prefs.getLong(key(driverId), NONE).takeIf { it != NONE }
    } catch (e: Exception) {
        Log.e(TAG, "Could not read last sync time", e)
        null
    }

    private fun key(driverId: String) = "last_synced_$driverId"

    private companion object {
        const val TAG = "SyncHistory"
        const val FILE_NAME = "sync_prefs"
        const val NONE = -1L
    }
}
