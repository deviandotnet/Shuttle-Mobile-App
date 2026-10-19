package com.example.shuttleapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.shuttleapp.data.local.PassengerQREntity
import com.example.shuttleapp.data.local.ShuttlePassEntity
import com.example.shuttleapp.data.local.ShuttlePassWithPassengerEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface ShuttlePassDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassenger(passenger: PassengerQREntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShuttlePass(shuttlePass: ShuttlePassEntity) : Long

    @Transaction
    @Query("DELETE from shuttle_pass WHERE id = :id")
    suspend fun deleteShuttlePassById(id: String)

    @Delete
    suspend fun deletePassenger(passenger: PassengerQREntity)

    @Update
    suspend fun updatePassenger(passenger: PassengerQREntity)

    @Update
    suspend fun updatePassengers(passengers: List<PassengerQREntity>)

    @Update
    suspend fun updateShuttlePass(shuttlePass: List<ShuttlePassEntity>)

    @Transaction
    @Query("SELECT * FROM passengerQR WHERE id = :id")
    fun getPassengerById(id : String) : Flow<PassengerQREntity>

    @Transaction
    @Query("SELECT * FROM passengerQR WHERE shuttlePassId = :shuttlePassId")
    fun getPassengersByForeignKeyId(shuttlePassId: String): Flow<List<PassengerQREntity>>

    @Transaction
    @Query("SELECT * FROM SHUTTLE_PASS WHERE id = :id")
    fun getShuttlePassById(id: String) : Flow<ShuttlePassEntity?>

    @Transaction
    @Query("SELECT * FROM shuttle_pass WHERE isDraft = 0 AND driver = :driverId")
    fun getAllShuttlePassWithPassengers(driverId: String) : Flow<List<ShuttlePassWithPassengerEntity>>

    @Transaction
    @Query("SELECT * FROM shuttle_pass WHERE isDraft = 0 AND isSynced = 0 AND driver = :driverId")
    fun getAllShuttlePassFilteredBySync(driverId: String) : Flow<List<ShuttlePassWithPassengerEntity>>

    @Transaction
    @Query("SELECT * FROM shuttle_pass WHERE isDraft = 0 AND isLateShuttle = 1 AND driver = :driverId")
    fun getAllShuttlePassFilteredByLate(driverId: String) : Flow<List<ShuttlePassWithPassengerEntity>>

    @Transaction
    @Query("SELECT * FROM shuttle_pass WHERE isDraft = 0 AND isSynced = 0 AND driver = :driverId")
    suspend fun getAllUnsyncedShuttlePasses(driverId: String) : List<ShuttlePassWithPassengerEntity>

    @Transaction
    @Query("SELECT * FROM shuttle_pass WHERE id = :id")
    fun getShuttlePassWithPassengerById(id: String) : Flow<ShuttlePassWithPassengerEntity?>

    @Query("UPDATE shuttle_pass SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Transaction
    @Query("SELECT * FROM shuttle_pass WHERE isDraft = 1 AND driver = :driverId LIMIT 1")
    fun getDraftedShuttlePass(driverId: String) : Flow<ShuttlePassEntity?>

    @Transaction
    @Query("SELECT id FROM shuttle_pass WHERE isDraft = 1 AND  driver = :driverId LIMIT 1")
    suspend fun getShuttlePassIdDraftByDriverId(driverId: String) : String?

    @Transaction
    @Query("SELECT COUNT(*) FROM shuttle_pass WHERE isLateShuttle = 1 AND isDraft = 0 AND driver = :accountId")
    fun countLateShuttle(accountId: String) : Flow<Int>

    @Transaction
    @Query("SELECT COUNT(*) FROM shuttle_pass WHERE isSynced = 0 AND isDraft = 0 AND driver = :accountId")
    fun countUnsyncedShuttlePass(accountId: String) : Flow<Int>

    @Transaction
    @Query("""DELETE FROM shuttle_pass WHERE dateCreated <= (strftime('%s', 'now', '-1 month') * 1000) AND driver = :driverId AND isSynced = 1""")
    suspend fun deleteShuttlePassOnDue(driverId: String) : Int

    @Transaction
    @Query("""DELETE FROM shuttle_pass WHERE dateCreated <= (strftime('%s', 'now', '-1 month') * 1000) AND driver = :driverId AND isDraft = 1""")
    suspend fun deleteAllDraftedShuttlePass(driverId: String) : Int

    @Transaction
    @Query("DELETE FROM shuttle_pass WHERE isSynced = 1 AND driver = :accountId")
    suspend fun deleteAllSyncedShuttlePass(accountId: String) : Int
}