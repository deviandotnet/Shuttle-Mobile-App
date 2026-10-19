package com.example.shuttleapp.domain.repository

import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {

    val session: StateFlow<SessionLoad>

    /** Called after a successful online login. [token] is null while the backend issues none. */
    suspend fun start(
        accountId: String,
        username: String,
        displayName: String,
        token: String?,
        expiresAtMillis: Long?
    )

    /** Signs the current driver in again (online) to refresh an expired token, keeping all local data. */
    suspend fun reauthenticate(username: String, password: String): Resource<Unit>

    fun markTokenExpired()

    /** Explicit logout: clears the session and token only; shuttle passes stay on the device. */
    suspend fun end()
}
