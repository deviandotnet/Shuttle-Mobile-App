package com.example.shuttleapp.presentation.events

sealed interface SyncAction {
    data object SyncNow : SyncAction
    data object DismissSyncResult : SyncAction
    data class OpenPass(val id: String) : SyncAction
    data object ShowReauth : SyncAction
    data class SubmitReauth(val username: String, val password: String) : SyncAction
    data object DismissReauth : SyncAction
}

sealed interface SyncEvent {
    data class NavigateToPass(val id: String) : SyncEvent
}
