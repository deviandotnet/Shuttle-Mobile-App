package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.ShuttleProvider
import com.example.shuttleapp.domain.repository.ShuttleProviderRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeShuttleProviderRepository(private val providers: List<ShuttleProvider> = emptyList()) : ShuttleProviderRepository {
    override suspend fun insertShuttleProvider(shuttleProvider: ShuttleProvider) = Unit
    override suspend fun getProviderNameById(id: String): Flow<String?> =
        flowOf(providers.firstOrNull { it.shuttleProviderId == id }?.providerName)
    override suspend fun getShuttleProviders(): Flow<Resource<List<ShuttleProvider>>> = flowOf(Resource.Success(providers))
}
