package com.example.shuttleapp.data.repositoryImpl

import com.example.shuttleapp.data.local.RememberedUserEntity
import com.example.shuttleapp.data.local.StoredSession
import com.example.shuttleapp.data.local.dao.RememberedUserDao
import com.example.shuttleapp.domain.model.AuthMessages
import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.domain.model.TokenStatus
import com.example.shuttleapp.testutil.FakeAuthApi
import com.example.shuttleapp.testutil.FakeSessionStore
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRepositoryImplTest {

    private val store = FakeSessionStore()
    private val authApi = FakeAuthApi()
    private val rememberedUserDao = FakeRememberedUserDao()

    private fun TestScope.repository(): SessionRepositoryImpl {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return SessionRepositoryImpl(store, authApi, rememberedUserDao, backgroundScope, dispatcher)
    }

    private fun SessionRepositoryImpl.signedIn() = (session.value as SessionLoad.SignedIn).session

    private fun stored(token: String? = null, expiresAt: Long? = null, expired: Boolean = false) =
        StoredSession("driver-1", "jdelacruz", "Juan Dela Cruz", token, expiresAt, expired)

    @Test
    fun `no stored session and no remembered user is signed out`() = runTest {
        assertEquals(SessionLoad.SignedOut, repository().session.value)
    }

    @Test
    fun `remembered user from the old table is migrated into a session`() = runTest {
        rememberedUserDao.remembered = RememberedUserEntity("driver-9", "E-9", "Ana", "", "Reyes", null, true, true)

        val session = repository().signedIn()

        assertEquals("driver-9", session.accountId)
        assertEquals("Ana Reyes", session.displayName)
        assertEquals(TokenStatus.None, session.tokenStatus)
    }

    @Test
    fun `start and end`() = runTest {
        val repository = repository()

        repository.start("driver-1", "jdelacruz", "Juan Dela Cruz", token = "t1", expiresAtMillis = null)
        assertEquals(TokenStatus.Valid, repository.signedIn().tokenStatus)

        repository.end()
        assertEquals(SessionLoad.SignedOut, repository.session.value)
        assertNull(store.state.value)
        assertTrue(rememberedUserDao.deleted)
    }

    @Test
    fun `token past its expiry or rejected by the server is expired but still signed in`() = runTest {
        store.write(stored(token = "t1", expiresAt = 1_000))
        val repository = repository().apply { now = { 2_000 } }
        store.write(stored(token = "t1", expiresAt = 1_000)) // re-emit with the test clock
        assertEquals(TokenStatus.Expired, repository.signedIn().tokenStatus)

        store.write(stored(token = "t2"))
        repository.markTokenExpired()
        assertEquals(TokenStatus.Expired, repository.signedIn().tokenStatus)
    }

    @Test
    fun `reauthenticate refreshes the token for the same account`() = runTest {
        store.write(stored(token = "old", expired = true))
        authApi.loginResponse = FakeAuthApi.success("driver-1", token = "new", expiresAt = "2030-01-01T00:00:00Z")
        val repository = repository()

        val result = repository.reauthenticate("jdelacruz", "secret")

        assertTrue(result is Resource.Success)
        assertEquals("new", store.state.value?.token)
        assertEquals(TokenStatus.Valid, repository.signedIn().tokenStatus)
    }

    @Test
    fun `reauthenticate rejects a different account and keeps the session`() = runTest {
        store.write(stored(token = "old", expired = true))
        authApi.loginResponse = FakeAuthApi.success("someone-else", token = "new")
        val repository = repository()

        val result = repository.reauthenticate("other", "secret")

        assertEquals(AuthMessages.DIFFERENT_ACCOUNT, result.message)
        assertEquals("old", store.state.value?.token)
        assertEquals(TokenStatus.Expired, repository.signedIn().tokenStatus)
    }

    @Test
    fun `reauthenticate offline reports that sign-in needs internet`() = runTest {
        store.write(stored(token = "old", expired = true))
        authApi.loginResponse = null
        val repository = repository()

        val result = repository.reauthenticate("jdelacruz", "secret")

        assertEquals(AuthMessages.SIGN_IN_NEEDS_INTERNET, result.message)
        assertEquals(TokenStatus.Expired, repository.signedIn().tokenStatus)
    }

    private class FakeRememberedUserDao : RememberedUserDao {
        var remembered: RememberedUserEntity? = null
        var deleted = false
        override suspend fun insertLoggedUser(loggedUserEntity: RememberedUserEntity) { remembered = loggedUserEntity }
        override fun getRememberedUser(): RememberedUserEntity? = remembered
        override fun getLoggerUser(): RememberedUserEntity? = remembered
        override suspend fun deleteLoggedUser() { deleted = true; remembered = null }
    }
}
