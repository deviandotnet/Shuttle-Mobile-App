package com.example.shuttleapp.domain.repository

import com.example.shuttleapp.domain.model.PassengerQR
import kotlinx.coroutines.flow.Flow

interface PassengerQRRepository {

    suspend fun getPassengerId(name: String, shuttlePassId: String) : String?

    fun getDraftedPassengers(shuttlePassId: String) : Flow<List<PassengerQR>>

    fun getPassengerById(id: String) : Flow<PassengerQR>

    suspend fun deletePassenger(passenger: PassengerQR)

    suspend fun updatePassenger(passenger: PassengerQR)

    suspend fun insertPassenger(passenger: PassengerQR)
}
