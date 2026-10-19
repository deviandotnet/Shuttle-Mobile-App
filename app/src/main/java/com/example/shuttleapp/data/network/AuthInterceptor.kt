package com.example.shuttleapp.data.network

import com.example.shuttleapp.config.API
import com.example.shuttleapp.data.local.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds the Bearer token to every call except login/register, and flags the token as expired on 401.
 * Depends on [SessionStore] rather than the session repository to avoid a Retrofit -> AuthApi DI cycle.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (path.endsWith(API.LOGIN_PATH) || path.endsWith(API.REGISTER_PATH)) {
            return chain.proceed(request)
        }

        val token = sessionStore.token() ?: return chain.proceed(request)
        val response = chain.proceed(
            request.newBuilder().header("Authorization", "Bearer $token").build()
        )
        if (response.code == 401) sessionStore.markTokenExpired()
        return response
    }
}
