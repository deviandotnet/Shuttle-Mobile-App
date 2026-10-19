package com.example.shuttleapp.domain.repository

import com.example.shuttleapp.domain.model.Shuttle
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface ShuttleRepository {

    suspend fun getShuttles() : List<Shuttle>

    suspend fun insertShuttles(shuttles: List<Shuttle>)

    suspend fun getShuttleByProviderId(providerId: String, forceFetchFromRemote: Boolean) : Flow<Resource<List<Shuttle>>>

    suspend fun getShuttleIdByPlateNumber(plateNumber: String): String

    suspend fun getPlateNumberById(id: String) : Flow<String?>

}
