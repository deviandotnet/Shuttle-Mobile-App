package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.PassFilter
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.presentation.events.ShuttlePassListAction
import com.example.shuttleapp.presentation.events.ShuttlePassListEvent
import com.example.shuttleapp.presentation.mapper.ShuttlePassItemUiMapper
import com.example.shuttleapp.presentation.state.ShuttlePassListState
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ShuttlePassListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shuttlePassRepository: ShuttlePassRepository,
    private val itemUiMapper: ShuttlePassItemUiMapper,
    uploadControllerFactory: PassUploadController.Factory
) : ViewModel() {

    private val driverId: String = savedStateHandle["id"] ?: ""

    /** Home's stat tiles open the list already filtered (route argument "filter"). */
    private val filter = MutableStateFlow(PassFilter.fromRoute(savedStateHandle.get<String>(FILTER_ARG)))
    private val upload = uploadControllerFactory.create(driverId, viewModelScope)

    private val _events = Channel<ShuttlePassListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // flatMapLatest cancels the previous filter's query, so a stale filter can't overwrite the list.
    private val items = filter
        .flatMapLatest { shuttlePassRepository.observePasses(driverId, it) }
        .map { passes -> itemUiMapper.toUi(passes) }

    val state: StateFlow<ShuttlePassListState> = combine(
        items,
        filter,
        upload.state,
        shuttlePassRepository.observeUnsyncedCount(driverId)
    ) { items, filter, upload, pending ->
        ShuttlePassListState(
            filter = filter,
            items = items,
            isLoading = false,
            sync = upload.sync,
            isOnline = upload.isOnline,
            needsSignIn = upload.needsSignIn,
            reauth = upload.reauth,
            pendingCount = pending
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShuttlePassListState(filter = filter.value))

    fun onAction(action: ShuttlePassListAction) {
        when (action) {
            is ShuttlePassListAction.SelectFilter -> filter.value = action.filter
            ShuttlePassListAction.UploadAll -> upload.uploadAll()
            ShuttlePassListAction.DismissSyncResult -> upload.dismissResult()
            is ShuttlePassListAction.OpenPass -> send(ShuttlePassListEvent.NavigateToPass(action.id))
            ShuttlePassListAction.Back -> send(ShuttlePassListEvent.NavigateBack)
            ShuttlePassListAction.ShowReauth -> upload.showReauth()
            is ShuttlePassListAction.SubmitReauth -> upload.submitReauth(action.username, action.password)
            ShuttlePassListAction.DismissReauth -> upload.dismissReauth()
        }
    }

    private fun send(event: ShuttlePassListEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    companion object {
        const val FILTER_ARG = "filter"
        const val OFFLINE_MESSAGE = PassUploadController.OFFLINE_MESSAGE
    }
}
