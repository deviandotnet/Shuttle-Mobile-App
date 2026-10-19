package com.example.shuttleapp.domain.repository

import com.example.shuttleapp.domain.model.ShuttleProvider
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface ShuttleProviderRepository {

    suspend fun insertShuttleProvider(shuttleProvider: ShuttleProvider)

    suspend fun getProviderNameById(id: String) : Flow<String?>

    suspend fun getShuttleProviders() : Flow<Resource<List<ShuttleProvider>>>
}
