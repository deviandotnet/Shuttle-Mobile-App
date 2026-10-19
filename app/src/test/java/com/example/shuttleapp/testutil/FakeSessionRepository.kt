package com.example.shuttleapp.testutil

import com.example.shuttleapp.domain.model.Session
import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.domain.model.TokenStatus
import com.example.shuttleapp.domain.repository.SessionRepository
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSessionRepository(
    initial: SessionLoad = SessionLoad.SignedIn(
        Session(accountId = "driver-1", username = "jdelacruz", displayName = "Juan Dela Cruz", tokenStatus = TokenStatus.None)
    )
) : SessionRepository {

    override val session = MutableStateFlow(initial)

    var reauthResult: Resource<Unit> = Resource.Success(Unit)
    var endCalls = 0
        private set
    val reauthAttempts = mutableListOf<Pair<String, String>>()

    fun setTokenStatus(status: TokenStatus) {
        session.update { (it as SessionLoad.SignedIn).copy(session = it.session.copy(tokenStatus = status)) }
    }

    override suspend fun start(accountId: String, username: String, displayName: String, token: String?, expiresAtMillis: Long?) {
        session.value = SessionLoad.SignedIn(
            Session(accountId, username, displayName, if (token == null) TokenStatus.None else TokenStatus.Valid)
        )
    }

    override suspend fun reauthenticate(username: String, password: String): Resource<Unit> {
        reauthAttempts += username to password
        if (reauthResult is Resource.Success) setTokenStatus(TokenStatus.Valid)
        return reauthResult
    }

    override fun markTokenExpired() = setTokenStatus(TokenStatus.Expired)

    override suspend fun end() {
        endCalls++
        session.value = SessionLoad.SignedOut
    }
}
