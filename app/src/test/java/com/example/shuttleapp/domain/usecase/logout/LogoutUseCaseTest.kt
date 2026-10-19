package com.example.shuttleapp.domain.usecase.logout

import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.domain.model.UserData
import com.example.shuttleapp.domain.repository.UserDataRepository
import com.example.shuttleapp.testutil.FakeSessionRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoutUseCaseTest {

    @Test
    fun `ends the session and clears the cached profile`() = runTest {
        val sessionRepository = FakeSessionRepository()
        val userDataRepository = FakeUserDataRepository()

        LogoutUseCase(sessionRepository, userDataRepository)()

        assertEquals(SessionLoad.SignedOut, sessionRepository.session.value)
        assertEquals(1, sessionRepository.endCalls)
        assertTrue(userDataRepository.cleared)
        // Shuttle passes are not touched: the use case has no dependency that could delete them.
    }

    private class FakeUserDataRepository : UserDataRepository {
        var cleared = false
        override suspend fun clearUserData() { cleared = true }
        override suspend fun getProviderIdByAccountId(accountId: String): String = ""
        override suspend fun getUserById(accountId: String): Flow<Resource<UserData>> = emptyFlow()
        override suspend fun getDriverNameById(id: String): Flow<String?> = emptyFlow()
    }
}
