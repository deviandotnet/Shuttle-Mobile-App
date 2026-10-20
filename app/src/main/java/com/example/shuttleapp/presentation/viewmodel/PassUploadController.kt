package com.example.shuttleapp.presentation.viewmodel

import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.domain.model.TokenStatus
import com.example.shuttleapp.domain.repository.NetworkMonitor
import com.example.shuttleapp.domain.repository.SessionRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.presentation.state.PassUploadState
import com.example.shuttleapp.presentation.state.ReauthState
import com.example.shuttleapp.presentation.state.SyncStatus
import com.example.shuttleapp.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The "upload all pending passes" flow shared by the pass list, the Sync tab, the Home callout and the drawer:
 * refuses while offline, asks the driver to sign in again when the token expired, and reports progress.
 * Each ViewModel owns one, created with its driver id and viewModelScope.
 */
class PassUploadController(
    private val driverId: String,
    private val scope: CoroutineScope,
    private val shuttlePassRepository: ShuttlePassRepository,
    private val sessionRepository: SessionRepository,
    networkMonitor: NetworkMonitor
) {
    private val syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    private val reauth = MutableStateFlow<ReauthState?>(null)

    // Eager, so uploadAll() can read the current connection state without a subscriber.
    val state: StateFlow<PassUploadState> =
        combine(networkMonitor.isOnline, sessionRepository.session, syncStatus, reauth) { online, session, sync, reauth ->
            PassUploadState(
                sync = sync,
                isOnline = online,
                needsSignIn = (session as? SessionLoad.SignedIn)?.session?.tokenStatus == TokenStatus.Expired,
                reauth = reauth
            )
        }.stateIn(scope, SharingStarted.Eagerly, PassUploadState())

    fun uploadAll() {
        if (syncStatus.value == SyncStatus.Running) return
        val current = state.value
        when {
            !current.isOnline -> syncStatus.value = SyncStatus.Failed(OFFLINE_MESSAGE)
            current.needsSignIn -> showReauth()
            else -> {
                syncStatus.value = SyncStatus.Running
                scope.launch {
                    syncStatus.value = when (val result = shuttlePassRepository.syncUnsynced(driverId)) {
                        is Resource.Success -> {
                            val count = result.data?.syncedCount ?: 0
                            SyncStatus.Success(result.data?.message ?: "Uploaded $count shuttle pass(es).")
                        }
                        else -> SyncStatus.Failed(result.message ?: "Upload failed. Please try again.")
                    }
                }
            }
        }
    }

    fun dismissResult() {
        syncStatus.value = SyncStatus.Idle
    }

    fun showReauth() {
        val username = (sessionRepository.session.value as? SessionLoad.SignedIn)?.session?.username.orEmpty()
        reauth.value = ReauthState(username = username)
    }

    fun dismissReauth() {
        reauth.value = null
    }

    fun submitReauth(username: String, password: String) {
        if (reauth.value?.isSubmitting == true) return
        if (username.isBlank() || password.isBlank()) {
            reauth.update { it?.copy(username = username, error = "Enter your username and password.") }
            return
        }
        reauth.update { it?.copy(username = username, isSubmitting = true, error = null) }
        scope.launch {
            when (val result = sessionRepository.reauthenticate(username, password)) {
                is Resource.Success -> reauth.value = null
                else -> reauth.update { it?.copy(isSubmitting = false, error = result.message ?: "Sign in failed.") }
            }
        }
    }

    class Factory @Inject constructor(
        private val shuttlePassRepository: ShuttlePassRepository,
        private val sessionRepository: SessionRepository,
        private val networkMonitor: NetworkMonitor
    ) {
        fun create(driverId: String, scope: CoroutineScope) =
            PassUploadController(driverId, scope, shuttlePassRepository, sessionRepository, networkMonitor)
    }

    companion object {
        const val OFFLINE_MESSAGE = "You're offline. Your passes stay saved on this device until you upload."
    }
}
