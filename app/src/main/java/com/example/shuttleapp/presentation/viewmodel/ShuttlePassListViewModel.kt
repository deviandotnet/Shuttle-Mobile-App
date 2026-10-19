package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger
import com.example.shuttleapp.domain.model.TokenStatus
import com.example.shuttleapp.domain.repository.NetworkMonitor
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.domain.repository.SessionRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.presentation.events.ShuttlePassListAction
import com.example.shuttleapp.presentation.events.ShuttlePassListEvent
import com.example.shuttleapp.presentation.state.ReauthState
import com.example.shuttleapp.presentation.state.ShuttlePassItemUi
import com.example.shuttleapp.presentation.state.ShuttlePassListState
import com.example.shuttleapp.presentation.state.SyncStatus
import com.example.shuttleapp.util.Resource
import com.example.shuttleapp.util.formatMonthYear
import com.example.shuttleapp.util.formatTimeHHmm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ShuttlePassListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shuttlePassRepository: ShuttlePassRepository,
    private val routesRepository: RoutesRepository,
    private val sessionRepository: SessionRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val driverId: String = savedStateHandle["id"] ?: ""

    private val filter = MutableStateFlow(PassFilter.All)
    private val syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    private val reauth = MutableStateFlow<ReauthState?>(null)
    private val routeNames = mutableMapOf<String, String>()

    private val _events = Channel<ShuttlePassListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // flatMapLatest cancels the previous filter's query, so a stale filter can't overwrite the list.
    private val items = filter
        .flatMapLatest { shuttlePassRepository.observePasses(driverId, it) }
        .map { passes -> passes.sortedByDescending { it.shuttlePass.dateCreated }.map { it.toUi() } }

    private val connection = combine(networkMonitor.isOnline, sessionRepository.session, reauth) { online, session, reauth ->
        val needsSignIn = (session as? SessionLoad.SignedIn)?.session?.tokenStatus == TokenStatus.Expired
        Connection(online, needsSignIn, reauth)
    }

    val state: StateFlow<ShuttlePassListState> = combine(items, filter, syncStatus, connection) { items, filter, sync, connection ->
        ShuttlePassListState(
            filter = filter,
            items = items,
            isLoading = false,
            sync = sync,
            isOnline = connection.isOnline,
            needsSignIn = connection.needsSignIn,
            reauth = connection.reauth
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShuttlePassListState())

    fun onAction(action: ShuttlePassListAction) {
        when (action) {
            is ShuttlePassListAction.SelectFilter -> filter.value = action.filter
            ShuttlePassListAction.UploadAll -> uploadAll()
            ShuttlePassListAction.DismissSyncResult -> syncStatus.value = SyncStatus.Idle
            is ShuttlePassListAction.OpenPass -> send(ShuttlePassListEvent.NavigateToPass(action.id))
            ShuttlePassListAction.Back -> send(ShuttlePassListEvent.NavigateBack)
            ShuttlePassListAction.ShowReauth -> showReauth()
            is ShuttlePassListAction.SubmitReauth -> submitReauth(action.username, action.password)
            ShuttlePassListAction.DismissReauth -> reauth.value = null
        }
    }

    private fun uploadAll() {
        if (syncStatus.value == SyncStatus.Running) return
        val current = state.value
        when {
            !current.isOnline -> syncStatus.value = SyncStatus.Failed(OFFLINE_MESSAGE)
            current.needsSignIn -> showReauth()
            else -> {
                syncStatus.value = SyncStatus.Running
                viewModelScope.launch {
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

    private fun showReauth() {
        val username = (sessionRepository.session.value as? SessionLoad.SignedIn)?.session?.username.orEmpty()
        reauth.value = ReauthState(username = username)
    }

    private fun submitReauth(username: String, password: String) {
        if (reauth.value?.isSubmitting == true) return
        if (username.isBlank() || password.isBlank()) {
            reauth.update { it?.copy(username = username, error = "Enter your username and password.") }
            return
        }
        reauth.update { it?.copy(username = username, isSubmitting = true, error = null) }
        viewModelScope.launch {
            when (val result = sessionRepository.reauthenticate(username, password)) {
                is Resource.Success -> reauth.value = null
                else -> reauth.update { it?.copy(isSubmitting = false, error = result.message ?: "Sign in failed.") }
            }
        }
    }

    private fun send(event: ShuttlePassListEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private suspend fun ShuttlePassWithPassenger.toUi(): ShuttlePassItemUi {
        val pass = shuttlePass
        return ShuttlePassItemUi(
            id = pass.id,
            code = "SPT-${formatMonthYear(pass.dateCreated)}-${pass.id.takeLast(12).uppercase()}",
            schedule = "${pass.date}  ${formatTimeHHmm(pass.departure)} — ${formatTimeHHmm(pass.arrival)}",
            routeAndTrip = "${routeName(pass.routeId)} · ${pass.tripType}",
            isLate = pass.isLateShuttle,
            isSynced = pass.isSynced
        )
    }

    private suspend fun routeName(routeId: String): String {
        routeNames[routeId]?.let { return it }
        // A missing route shows a blank name rather than failing the whole list; it is retried next emission.
        return try {
            routesRepository.getRouteNameById(routeId).also { routeNames[routeId] = it }
        } catch (ex: Exception) {
            ""
        }
    }

    private data class Connection(val isOnline: Boolean, val needsSignIn: Boolean, val reauth: ReauthState?)

    companion object {
        const val OFFLINE_MESSAGE = "You're offline. Your passes stay saved on this device until you upload."
    }
}
