package com.example.shuttleapp.data.network

import com.example.shuttleapp.data.local.StoredSession
import com.example.shuttleapp.testutil.FakeSessionStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val server = MockWebServer()
    private val store = FakeSessionStore()
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server.start()
        client = OkHttpClient.Builder().addInterceptor(AuthInterceptor(store)).build()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun signIn(token: String?) =
        store.write(StoredSession("driver-1", "jdelacruz", "Juan", token, null, tokenExpired = false))

    private fun call(path: String): Int {
        server.enqueue(MockResponse().setResponseCode(if (path.contains("fail")) 401 else 200))
        return client.newCall(Request.Builder().url(server.url("/api/$path")).build()).execute().use { it.code }
    }

    @Test
    fun `adds the bearer token to api calls`() {
        signIn("abc")
        call("shuttlepasses/")
        assertEquals("Bearer abc", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `does not send the token to login or register`() {
        signIn("abc")
        call("users/login/")
        call("users/register/")
        assertNull(server.takeRequest().getHeader("Authorization"))
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `no token means no header`() {
        signIn(null)
        call("shuttlepasses/")
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `401 marks the token expired without signing out`() {
        signIn("abc")
        assertEquals(401, call("shuttlepasses/fail"))
        assertTrue(store.state.value!!.tokenExpired)
        assertEquals("driver-1", store.state.value!!.accountId)
    }

    @Test
    fun `401 without a token changes nothing`() {
        signIn(null)
        call("shuttlepasses/fail")
        assertFalse(store.state.value!!.tokenExpired)
    }
}
