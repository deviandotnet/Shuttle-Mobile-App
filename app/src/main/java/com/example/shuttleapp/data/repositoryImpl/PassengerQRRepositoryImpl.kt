package com.example.shuttleapp.data.repositoryImpl

import com.example.shuttleapp.config.ShuttleDatabase
import com.example.shuttleapp.data.mappers.toDomain
import com.example.shuttleapp.data.mappers.toEntity
import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PassengerQRRepositoryImpl @Inject constructor(
    private val database: ShuttleDatabase
) : PassengerQRRepository {

    //upsert : create-update
    override suspend fun insertPassenger(passenger: PassengerQR) {
        database.PassengerQRDao().insertPassengerQR(passenger.toEntity())
    }
    //edit
    override suspend fun updatePassenger(passenger: PassengerQR) {
        database.ShuttlePassDao().updatePassenger(passenger.toEntity())
    }
    //delete
    override suspend fun deletePassenger(passenger: PassengerQR) {
        database.ShuttlePassDao().deletePassenger(passenger.toEntity())
    }

    override fun getDraftedPassengers(shuttlePassId: String) : Flow<List<PassengerQR>> {
        return database.PassengerQRDao().getDraftedPassengers(shuttlePassId)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getPassengerById(id: String) : Flow<PassengerQR> {
        return database.ShuttlePassDao().getPassengerById(id).map { it.toDomain() }
    }

    override suspend fun getPassengerId(name: String, shuttlePassId: String) : String? {
        return database.PassengerQRDao().getPassengerId(name, shuttlePassId)
    }
}
