package com.example.shuttleapp.data.repositoryImpl

import android.util.Log
import com.example.shuttleapp.data.mappers.toEntity
import com.example.shuttleapp.data.mappers.toShuttle
import com.example.shuttleapp.data.mappers.toShuttleEntity
import com.example.shuttleapp.domain.model.Shuttle
import com.example.shuttleapp.domain.repository.ShuttleRepository
import com.example.shuttleapp.config.ShuttleDatabase
import com.example.shuttleapp.data.network.ShuttleApi
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ShuttleRepositoryImpl @Inject constructor(
    private val shuttleApi: ShuttleApi,
    private val database: ShuttleDatabase
) : ShuttleRepository
{

    override suspend fun getShuttles(): List<Shuttle> {
        val localShuttle = database.ShuttleDao().getAllShuttle()
        return localShuttle.map { it.toShuttle() }
    }

    override suspend fun insertShuttles(shuttles: List<Shuttle>) {
        database.ShuttleDao().insertShuttle(shuttles.map { it.toEntity() })
    }

    override suspend fun getShuttleByProviderId(providerId: String, forceFetchFromRemote: Boolean): Flow<Resource<List<Shuttle>>> {

        return flow {
            emit(Resource.Loading(true))

            val localShuttle = database.ShuttleDao().getShuttlesByProviderId(providerId)

            if(localShuttle.isNotEmpty() && !forceFetchFromRemote) {
                emit(Resource.Success(
                    data = localShuttle.map { it.toShuttle() }
                ))
                emit(Resource.Loading(false))
            }

//            //get data through network
            try{
                val shuttlesFromApi = shuttleApi.getShuttlesByProviderId(providerId)
                val shuttleEntity = shuttlesFromApi.map { it.toShuttleEntity() }

                if(localShuttle != shuttleEntity) {

                    //database.ShuttleDao().clearAllShuttles()
                    database.ShuttleDao().insertShuttle(shuttleEntity)
                    Log.i("FetchShuttle", shuttleEntity.toString())
                    //emit the flow for consumption
                    emit(Resource.Success(data = shuttleEntity.map { it.toShuttle() }))
                }
            } catch (ex: IOException) {
                Log.e("FetchShuttleIO", ex.message.toString())
                emit(Resource.Error("error fetching shuttles from api"))
            } catch (ex: HttpException) {
                Log.e("FetchShuttleHttp", ex.message.toString())
                emit(Resource.Error("error fetching shuttles from api"))
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                Log.e("FetchShuttle", ex.message.toString())
                emit(Resource.Error("error fetching shuttles from api"))
            } finally {
                emit(Resource.Loading(false))
            }


        }

    }

    override suspend fun getShuttleIdByPlateNumber(plateNumber: String): String {
        return database.ShuttleDao().getShuttleIdByPlateNumber(plateNumber)
    }

    override suspend fun getPlateNumberById(id: String): Flow<String?> {
        return flow {
            val plateNumber = database.ShuttleDao().getPlateNumberById(id)
            emit(plateNumber ?: "")
        }.flowOn(Dispatchers.IO)
    }


}