package com.example.shuttleapp.data.repositoryImpl

import com.example.shuttleapp.data.local.SessionStore
import com.example.shuttleapp.data.local.StoredSession
import com.example.shuttleapp.data.local.dao.RememberedUserDao
import com.example.shuttleapp.data.network.AuthApi
import com.example.shuttleapp.data.network.request.LoginRequestBodyDto
import com.example.shuttleapp.di.ApplicationScope
import com.example.shuttleapp.di.IoDispatcher
import com.example.shuttleapp.domain.model.AuthMessages
import com.example.shuttleapp.domain.model.Session
import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.domain.model.TokenStatus
import com.example.shuttleapp.domain.repository.SessionRepository
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.parseIsoUtcMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val store: SessionStore,
    private val authApi: AuthApi,
    private val rememberedUserDao: RememberedUserDao,
    @ApplicationScope scope: CoroutineScope,
    @IoDispatcher private val io: CoroutineDispatcher
) : SessionRepository {

    internal var now: () -> Long = System::currentTimeMillis

    private val _session = MutableStateFlow<SessionLoad>(SessionLoad.Loading)
    override val session: StateFlow<SessionLoad> = _session.asStateFlow()

    init {
        scope.launch(io) {
            try {
                migrateRememberedUser()
                store.state.collect { _session.value = it.toSessionLoad() }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                // Never leave the app stuck on the loading screen (or crash it) because the store is unreadable:
                // the driver can still sign in, and their passes in the database are untouched.
                _session.value = SessionLoad.SignedOut
            }
        }
    }

    override suspend fun start(
        accountId: String,
        username: String,
        displayName: String,
        token: String?,
        expiresAtMillis: Long?
    ) = withContext(io) {
        store.write(StoredSession(accountId, username, displayName, token, expiresAtMillis, tokenExpired = false))
    }

    override suspend fun reauthenticate(username: String, password: String): Resource<Unit> {
        val current = withContext(io) { store.state.value }
            ?: return Resource.Error("No driver is signed in on this device.")
        return try {
            val response = withContext(io) { authApi.postLogin(LoginRequestBodyDto(username, password)) }
            val body = response.body()
            when {
                !response.isSuccessful -> Resource.Error("Sign in failed. ${response.message()}")
                body?.message != AuthApi.LOGIN_SUCCESS_MESSAGE -> Resource.Error("Sign in failed. ${body?.message.orEmpty()}")
                body.userData?.id != current.accountId -> Resource.Error(AuthMessages.DIFFERENT_ACCOUNT)
                else -> {
                    withContext(io) {
                        store.write(
                            current.copy(
                                username = username,
                                token = body.token,
                                expiresAtMillis = parseIsoUtcMillis(body.expiresAt),
                                tokenExpired = false
                            )
                        )
                    }
                    Resource.Success(Unit)
                }
            }
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: IOException) {
            Resource.Error(AuthMessages.SIGN_IN_NEEDS_INTERNET)
        } catch (ex: Exception) {
            Resource.Error("Sign in failed: ${ex.localizedMessage}")
        }
    }

    override fun markTokenExpired() = store.markTokenExpired()

    override suspend fun end() = withContext(io) {
        store.clear()
        rememberedUserDao.deleteLoggedUser()
    }

    // Drivers who used "Remember me" before sessions existed stay signed in after the update.
    // The old table never stored the login username, so the re-auth dialog asks for it.
    private suspend fun migrateRememberedUser() {
        if (store.state.value != null) return
        val remembered = try {
            rememberedUserDao.getRememberedUser()
        } catch (ex: Exception) {
            null
        } ?: return
        store.write(
            StoredSession(
                accountId = remembered.accountId,
                username = "",
                displayName = "${remembered.firstName} ${remembered.lastName}".trim(),
                token = null,
                expiresAtMillis = null,
                tokenExpired = false
            )
        )
        // The legacy table holds driver PII; once the session lives in the encrypted store it is no longer needed.
        rememberedUserDao.deleteLoggedUser()
    }

    private fun StoredSession?.toSessionLoad(): SessionLoad {
        if (this == null) return SessionLoad.SignedOut
        val status = when {
            token == null -> TokenStatus.None
            tokenExpired || (expiresAtMillis != null && now() >= expiresAtMillis) -> TokenStatus.Expired
            else -> TokenStatus.Valid
        }
        return SessionLoad.SignedIn(Session(accountId, username, displayName, status))
    }
}
