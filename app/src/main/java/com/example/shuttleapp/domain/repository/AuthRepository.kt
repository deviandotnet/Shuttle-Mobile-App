package com.example.shuttleapp.domain.repository

import com.example.shuttleapp.domain.model.LoginCredentials
import com.example.shuttleapp.domain.model.Registration
import com.example.shuttleapp.domain.model.UserData
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun postLogin(credentials: LoginCredentials) : Flow<Resource<UserData>>

    suspend fun postRegister(registration: Registration): Flow<Resource<String>>
}
