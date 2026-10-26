package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.Shuttle
import com.example.shuttleapp.domain.repository.ShuttleRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeShuttleRepository(private val shuttles: List<Shuttle> = emptyList()) : ShuttleRepository {
    override suspend fun getShuttles(): List<Shuttle> = shuttles
    override suspend fun insertShuttles(shuttles: List<Shuttle>) = Unit
    override suspend fun getShuttleByProviderId(providerId: String, forceFetchFromRemote: Boolean): Flow<Resource<List<Shuttle>>> =
        flowOf(Resource.Success(shuttles.filter { it.shuttleProviderId == providerId }))
    override suspend fun getShuttleIdByPlateNumber(plateNumber: String): String =
        shuttles.firstOrNull { it.plateNumber == plateNumber }?.shuttleId ?: ""
    override suspend fun getPlateNumberById(id: String): Flow<String?> =
        flowOf(shuttles.firstOrNull { it.shuttleId == id }?.plateNumber)
}
