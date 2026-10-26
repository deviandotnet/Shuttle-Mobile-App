package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.UserData
import com.example.shuttleapp.domain.repository.UserDataRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserDataRepository(private val user: UserData? = null) : UserDataRepository {
    override suspend fun getProviderIdByAccountId(accountId: String): String = user?.providerId.orEmpty()
    override suspend fun getUserById(accountId: String): Flow<Resource<UserData>> =
        if (user == null) flowOf(Resource.Error("No user $accountId")) else flowOf(Resource.Success(user))
    override suspend fun clearUserData() = Unit
    /** Like the real one, emits nothing when the driver isn't stored. */
    override suspend fun getDriverNameById(id: String): Flow<String?> =
        if (user?.accountId == id) flowOf("${user.firstName} ${user.lastName}") else emptyFlow()
}
