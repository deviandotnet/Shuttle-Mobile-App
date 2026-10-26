package com.example.shuttleapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.shuttleapp.data.local.PassengerQREntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PassengerQRDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassengerQR(passengerQR: PassengerQREntity) : Long

    @Delete
    suspend fun deletePassengerQR(passengerQR: PassengerQREntity)

    @Update
    suspend fun updatePassengerQR(passengerQR: PassengerQREntity)

    @Update
    suspend fun updatePassengersQR(passengersQR: List<PassengerQREntity>)

    @Transaction
    @Query("SELECT * FROM passengerQR WHERE id = :id")
    fun getPassengerQRById(id : String) : Flow<PassengerQREntity>

    @Transaction
    @Query("SELECT * FROM passengerQR WHERE shuttlePassId = :shuttlePassId ORDER BY rowid")
    fun getPassengersQRByForeignKeyId(shuttlePassId: String): Flow<List<PassengerQREntity>>

    @Transaction
    @Query("SELECT * FROM passengerQR WHERE isDraft = 1 ORDER BY id DESC")
    fun getDraftedPassengersQR() : Flow<List<PassengerQREntity>>

    @Transaction
    @Query("SELECT id FROM passengerQR WHERE scannedQR = :scannedQR AND shuttlePassId = :shuttlePassId")
    suspend fun getPassengerQRId(scannedQR: String, shuttlePassId: String) : String?

    @Transaction
    @Query("SELECT id FROM shuttle_pass WHERE isDraft = 1 ")
    suspend fun getDraftId() : String?

    @Transaction
    @Query("SELECT COUNT(*) FROM shuttle_pass WHERE isSynced = 0")
    fun countCurrentPassenger() : Flow<Int>

    @Transaction
    // rowid is the insert order (the id is a UUID), so this is boarding order even across midnight.
    @Query("SELECT * FROM passengerQR WHERE isDraft = 1 AND shuttlePassId = :shuttlePassId ORDER BY rowid")
    fun getDraftedPassengers(shuttlePassId: String) : Flow<List<PassengerQREntity>>

    @Transaction
    @Query("SELECT id FROM passengerQR WHERE scannedQR = :name AND shuttlePassId = :shuttlePassId")
    suspend fun getPassengerId(name: String, shuttlePassId: String) : String?

    @Transaction
    @Query("SELECT scannedQR FROM passengerQR WHERE isDraft = 1 AND shuttlePassId = :shuttlePassId")
    fun getScannedQQRCodes(shuttlePassId: String) : Flow<List<String>>
}