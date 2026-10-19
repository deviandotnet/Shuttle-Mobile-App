package com.example.shuttleapp.testutil

import com.example.shuttleapp.data.local.SessionStore
import com.example.shuttleapp.data.local.StoredSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSessionStore(initial: StoredSession? = null) : SessionStore {

    override val state = MutableStateFlow(initial)

    override fun write(session: StoredSession) {
        state.value = session
    }

    override fun markTokenExpired() {
        if (state.value?.token == null) return
        state.update { it?.copy(tokenExpired = true) }
    }

    override fun clear() {
        state.value = null
    }
}
