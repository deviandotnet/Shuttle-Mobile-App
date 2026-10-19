package com.example.shuttleapp.data.repositoryImpl

import android.util.Log
import com.example.shuttleapp.data.mappers.toEntity
import com.example.shuttleapp.data.mappers.toShuttleProviderEntity
import com.example.shuttleapp.data.mappers.toUserData
import com.example.shuttleapp.data.network.AuthApi
import com.example.shuttleapp.data.mappers.toDto
import com.example.shuttleapp.domain.model.LoginCredentials
import com.example.shuttleapp.domain.model.Registration
import com.example.shuttleapp.domain.model.UserData
import com.example.shuttleapp.domain.repository.AuthRepository
import com.example.shuttleapp.config.ShuttleDatabase
import com.example.shuttleapp.domain.model.AuthMessages
import com.example.shuttleapp.domain.repository.SessionRepository
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.parseIsoUtcMillis
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val database: ShuttleDatabase,
    private val sessionRepository: SessionRepository,
    //private val shuttleApi: ShuttleApi
) : AuthRepository {

    override suspend fun postLogin(credentials: LoginCredentials): Flow<Resource<UserData>> {

        return flow {
            emit(Resource.Loading(true))

            try {

                val response = authApi.postLogin(credentials.toDto())

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body?.message == AuthApi.LOGIN_SUCCESS_MESSAGE) {

                        //SAVE SHUTTLE PROVIDER
                        val shuttleProviderEntity = body.userData?.shuttleProvider?.toShuttleProviderEntity()
                        //insert first the shuttle provider
                        shuttleProviderEntity?.let { shuttleProvider ->
                            try {
                                database.ShuttleProviderDao().insertShuttleProvider(shuttleProvider)
                                Log.i("insertProvider", "shuttle provider inserted: $shuttleProviderEntity")
                            }catch (ex: Exception) {
                                ex.printStackTrace()
                                Log.e("insertProvider", "error inserting shuttleprovider:" + ex.message)
                            }
                        }

//                        try {
//                            //SAVE SHUTTLES (VEHICLES)
//                            val providerId = body.userdata?.providerid?.providerid
//
//                            providerId?.let{ it ->
//                                val shuttleFromApi = shuttleApi.getShuttlesByProviderId(it)
//
//                                val shuttleEntity = shuttleFromApi.map { it.toShuttleEntity() }
//                                database.ShuttleDao().clearAllShuttles()
//                                database.ShuttleDao().insertShuttle(shuttleEntity)
//                                Log.i("insertShuttle", "shuttle inserted: $shuttleEntity")
//
//                            }
//
//                        }catch (ex: Exception) {
//                            ex.printStackTrace()
//                            Log.e("insertShuttle", "error while managing the shuttles")
//                        }

                        // SAVE USER DATA
                        val userDataEntity = body.userData?.toEntity()
                        userDataEntity?.let { data ->
                            try{

                                database.UserDataDao().insertUserData(data)
                                Log.i("insertUserData", "user data inserted: $userDataEntity")

                            }
                            //android.database.sqlite.SQLiteConstraintException: FOREIGN KEY constraint failed (code 787 SQLITE_CONSTRAINT_FOREIGNKEY)
                            catch (ex:Exception) {
                                ex.printStackTrace()
                                Log.e("insertUserData", "error")
                            }
                        }
                        if (userDataEntity == null || userDataEntity.accountId.isEmpty()) {
                            emit(Resource.Error("Login failed. The server returned no account."))
                        } else {
                            // The session keeps the driver signed in offline until an explicit logout.
                            // Saved separately so a device storage failure isn't reported as a network error.
                            val sessionSaved = try {
                                sessionRepository.start(
                                    accountId = userDataEntity.accountId,
                                    username = credentials.username,
                                    displayName = "${userDataEntity.firstName} ${userDataEntity.lastName}".trim(),
                                    token = body.token,
                                    expiresAtMillis = parseIsoUtcMillis(body.expiresAt)
                                )
                                true
                            } catch (ex: CancellationException) {
                                throw ex
                            } catch (ex: Exception) {
                                Log.e("startSession", "Could not save the session", ex)
                                false
                            }
                            if (sessionSaved) {
                                emit(Resource.Success(userDataEntity.toUserData()))
                            } else {
                                emit(Resource.Error("Signed in, but this device couldn't save the sign-in. Please try again."))
                            }
                        }

                    } else {
                        emit(Resource.Error("Login failed. ${body?.message}"))
                    }
                } else {
                    emit(Resource.Error("Login error: ${response.message()}"))
                }
            }
            catch (ex: IOException) {
                emit(Resource.Error(AuthMessages.SIGN_IN_NEEDS_INTERNET))
            }
            catch (ex: Exception) {
                //emit(Resource.Error("Exception: ${ex.localizedMessage}"))
                emit(Resource.Error("Connection failed. Cannot connect to server."))
                ex.printStackTrace()
            }


        }

    }

    override suspend fun postRegister(registration: Registration) : Flow<Resource<String>> {

        return flow {
            emit(Resource.Loading(true))

            try {
                val response = authApi.postRegister(registration.toDto())

                if(response.isSuccessful) {

                    val body = response.body()

                    if(body?.status == 400) {
                        emit(Resource.Error(body.title))
                    }
                    else {
                        emit(Resource.Success(body?.title))
                    }
                } else {
                    // ASP.NET validation errors come back as non-2xx; without this the screen stays on loading.
                    emit(Resource.Error("Registration failed. ${response.message().ifBlank { "Error ${response.code()}" }}"))
                }

            } catch (ex: IOException) {
                emit(Resource.Error(AuthMessages.SIGN_IN_NEEDS_INTERNET))
            } catch (ex: Exception) {
                emit(Resource.Error("Connection failed. Cannot connect to server."))
                ex.printStackTrace()
            }
        }


    }
}