package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.PassengerQR
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory passengers; every pass id sees the same list. */
class FakePassengerQRRepository : PassengerQRRepository {

    val passengers = MutableStateFlow<List<PassengerQR>>(emptyList())

    override suspend fun getPassengerId(name: String, shuttlePassId: String): String? =
        passengers.value.firstOrNull { it.scannedQR == name }?.id

    override fun getDraftedPassengers(shuttlePassId: String): Flow<List<PassengerQR>> = passengers

    override fun getPassengerById(id: String): Flow<PassengerQR> = passengers.map { list -> list.first { it.id == id } }

    override suspend fun deletePassenger(passenger: PassengerQR) {
        passengers.value = passengers.value.filterNot { it.id == passenger.id }
    }

    override suspend fun updatePassenger(passenger: PassengerQR) {
        passengers.value = passengers.value.map { if (it.id == passenger.id) passenger else it }
    }

    override suspend fun insertPassenger(passenger: PassengerQR) {
        passengers.value = passengers.value + passenger
    }
}
