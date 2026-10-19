package com.example.shuttleapp.data.repositoryImpl

import androidx.room.withTransaction
import com.example.shuttleapp.config.ShuttleDatabase
import com.example.shuttleapp.data.local.ShuttlePassWithPassengerEntity
import com.example.shuttleapp.data.mappers.toDomain
import com.example.shuttleapp.data.mappers.toDto
import com.example.shuttleapp.data.mappers.toEntity
import com.example.shuttleapp.data.network.ShuttleApi
import com.example.shuttleapp.domain.model.AuthMessages
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.model.ShuttlePass
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.model.SyncResult
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class ShuttlePassRepositoryImpl @Inject constructor(
    private val api : ShuttleApi,
    private val database: ShuttleDatabase
) : ShuttlePassRepository {

    private val dao get() = database.ShuttlePassDao()

    override fun observePasses(driverId: String, filter: PassFilter): Flow<List<ShuttlePassWithPassenger>> {
        val source = when (filter) {
            PassFilter.All -> dao.getAllShuttlePassWithPassengers(driverId)
            PassFilter.Late -> dao.getAllShuttlePassFilteredByLate(driverId)
            PassFilter.NotSynced -> dao.getAllShuttlePassFilteredBySync(driverId)
        }
        return source.map { list -> list.map { it.toDomain() } }
    }

    override fun observePass(id: String): Flow<ShuttlePassWithPassenger?> {
        return dao.getShuttlePassWithPassengerById(id).map { it?.toDomain() }
    }

    override fun observeShuttlePass(id: String): Flow<ShuttlePass?> {
        return dao.getShuttlePassById(id).map { it?.toDomain() }
    }

    override fun observeDraft(driverId: String): Flow<ShuttlePass?> {
        return dao.getDraftedShuttlePass(driverId).map { it?.toDomain() }
    }

    override fun observeUnsyncedCount(driverId: String): Flow<Int> {
        return dao.countUnsyncedShuttlePass(driverId)
    }

    override fun observeLateCount(driverId: String): Flow<Int> {
        return dao.countLateShuttle(driverId)
    }

    override suspend fun getDraftId(driverId: String): String? {
        return dao.getShuttlePassIdDraftByDriverId(driverId)
    }

    override suspend fun insert(pass: ShuttlePass): Resource<Unit> = runDb("saving the shuttle pass") {
        dao.insertShuttlePass(pass.toEntity())
    }

    override suspend fun update(passes: List<ShuttlePass>): Resource<Unit> = runDb("updating the shuttle pass") {
        dao.updateShuttlePass(passes.map { it.toEntity() })
    }

    override suspend fun delete(id: String): Resource<Unit> = runDb("deleting the shuttle pass") {
        dao.deleteShuttlePassById(id)
    }

    override suspend fun finalizeDraft(
        driverId: String,
        pass: ShuttlePass,
        passengers: List<PassengerQR>
    ): Resource<Unit> {
        return try {
            val draftId = dao.getShuttlePassIdDraftByDriverId(driverId)
                ?: return Resource.Error("No active shuttle pass to complete.")

            database.withTransaction {
                dao.updateShuttlePass(listOf(pass.toEntity().copy(id = draftId, isDraft = false)))
                dao.updatePassengers(passengers.map { it.toEntity().copy(shuttlePassId = draftId, isDraft = false) })
            }
            Resource.Success(Unit)
        } catch (ex: Exception) {
            Resource.Error("Error completing the shuttle pass: ${ex.localizedMessage}")
        }
    }

    override suspend fun syncUnsynced(driverId: String): Resource<SyncResult> {
        val unsynced = try {
            dao.getAllUnsyncedShuttlePasses(driverId)
        } catch (ex: Exception) {
            return Resource.Error("Error loading unsynced shuttle passes: ${ex.localizedMessage}")
        }
        if (unsynced.isEmpty()) return Resource.Error("No unsynced shuttle pass")
        return upload(unsynced)
    }

    override suspend fun syncPass(id: String): Resource<SyncResult> {
        val pass = try {
            dao.getShuttlePassWithPassengerById(id).first()
        } catch (ex: Exception) {
            return Resource.Error("Error loading the shuttle pass: ${ex.localizedMessage}")
        }
        if (pass == null) return Resource.Error("Shuttle pass not found.")
        return upload(listOf(pass))
    }

    override suspend fun purgeExpired(driverId: String): Resource<Int> {
        return try {
            Resource.Success(dao.deleteShuttlePassOnDue(driverId) + dao.deleteAllDraftedShuttlePass(driverId))
        } catch (ex: Exception) {
            Resource.Error("Error deleting expired shuttle passes: ${ex.localizedMessage}")
        }
    }

    // Posts the given passes and marks only those ids as synced, so a pass finalized mid-request stays unsynced.
    private suspend fun upload(passes: List<ShuttlePassWithPassengerEntity>): Resource<SyncResult> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.postShuttlePassWithPassenger(passes.map { it.toDto() })
            }
            if (response.isSuccessful) {
                val ids = passes.map { it.shuttlePass.id }
                dao.markSynced(ids)
                Resource.Success(SyncResult(syncedCount = ids.size, message = response.body()?.message))
            } else if (response.code() == 401) {
                // AuthInterceptor has already flagged the token as expired; the passes stay unsynced.
                Resource.Error(AuthMessages.SESSION_EXPIRED)
            } else {
                Resource.Error("Failed. Status code: ${response.message()}")
            }
        } catch (ex: SocketTimeoutException) {
            Resource.Error("There is something a problem. The server may be offline or experiencing downtime. Please try again later.")
        } catch (ex: ConnectException) {
            Resource.Error("Cannot connect to network. Please check your internet connection and try again later.")
        } catch (ex: Exception) {
            Resource.Error("Exception: ${ex.localizedMessage ?: "Internal server error.Please try again later."}")
        }
    }

    private suspend fun runDb(action: String, block: suspend () -> Unit): Resource<Unit> {
        return try {
            block()
            Resource.Success(Unit)
        } catch (ex: Exception) {
            Resource.Error("Error $action: ${ex.localizedMessage}")
        }
    }
}
