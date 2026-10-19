package com.example.shuttleapp.data.repositoryImpl

import android.util.Log
import com.example.shuttleapp.data.mappers.toEntity
import com.example.shuttleapp.data.mappers.toShuttleProvider
import com.example.shuttleapp.data.mappers.toShuttleProviderEntity
import com.example.shuttleapp.domain.model.ShuttleProvider
import com.example.shuttleapp.domain.repository.ShuttleProviderRepository
import com.example.shuttleapp.config.ShuttleDatabase
import com.example.shuttleapp.data.network.ShuttleApi
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ShuttleProviderRepositoryImpl @Inject constructor(
    private val database: ShuttleDatabase,
    private val api: ShuttleApi
) : ShuttleProviderRepository{

    override suspend fun insertShuttleProvider(shuttleProvider: ShuttleProvider) {

        try {
            database.ShuttleProviderDao().insertShuttleProvider(shuttleProvider.toEntity())
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e("ShuttleProviderRepositoryImpl_insert", "${ex.message}")
        }
    }

    override suspend fun getProviderNameById(id: String): Flow<String?> {

        return flow{
            val provider = database.ShuttleProviderDao().getProviderNameById(id)
            emit(provider ?: "")
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getShuttleProviders(): Flow<Resource<List<ShuttleProvider>>> {

        return flow {
            emit(Resource.Loading(true))

            val shuttleProviders = api.getAllShuttleProviders()

            if(shuttleProviders != null) {
                emit(Resource.Success(data = shuttleProviders.map { shuttleProvider ->
                    shuttleProvider.toShuttleProviderEntity().toShuttleProvider()
                }))

                emit(Resource.Loading(false))
                return@flow
            }

            emit(Resource.Error("Error. No Shuttle Provider available"))
            emit(Resource.Loading(false))

        }
    }
}

