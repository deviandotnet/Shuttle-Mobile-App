package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.domain.repository.SyncHistoryRepository
import com.example.shuttleapp.presentation.events.SyncAction
import com.example.shuttleapp.presentation.events.SyncEvent
import com.example.shuttleapp.presentation.mapper.DashboardFormat
import com.example.shuttleapp.presentation.mapper.ShuttlePassItemUiMapper
import com.example.shuttleapp.presentation.state.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Passes waiting to upload and the "Sync now" action. Backs the Sync tab, and on Home the pending-sync
 * callout, the drawer's sync card and the bottom bar badge (both routes carry the driver id as "id").
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    shuttlePassRepository: ShuttlePassRepository,
    syncHistoryRepository: SyncHistoryRepository,
    private val itemUiMapper: ShuttlePassItemUiMapper,
    uploadControllerFactory: PassUploadController.Factory
) : ViewModel() {

    private val driverId: String = savedStateHandle["id"] ?: ""
    private val upload = uploadControllerFactory.create(driverId, viewModelScope)

    private val _events = Channel<SyncEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val pending = shuttlePassRepository.observePasses(driverId, PassFilter.NotSynced)
        .map { itemUiMapper.toUi(it) }

    // Formatted when the time changes; "today" can go stale past midnight, which re-entering the screen fixes.
    private val lastSynced = syncHistoryRepository.observeLastSyncedAt(driverId)
        .map { DashboardFormat.lastSynced(it, System.currentTimeMillis()) }

    val state: StateFlow<SyncState> = combine(pending, lastSynced, upload.state) { pending, lastSynced, upload ->
        SyncState(pending = pending, isLoading = false, lastSynced = lastSynced, upload = upload)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncState())

    fun onAction(action: SyncAction) {
        when (action) {
            SyncAction.SyncNow -> upload.uploadAll()
            SyncAction.DismissSyncResult -> upload.dismissResult()
            is SyncAction.OpenPass -> viewModelScope.launch { _events.send(SyncEvent.NavigateToPass(action.id)) }
            SyncAction.ShowReauth -> upload.showReauth()
            is SyncAction.SubmitReauth -> upload.submitReauth(action.username, action.password)
            SyncAction.DismissReauth -> upload.dismissReauth()
        }
    }
}
