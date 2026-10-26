package com.example.shuttleapp.presentation.events

import com.example.shuttleapp.domain.model.PassFilter

sealed interface ShuttlePassListAction {
    data class SelectFilter(val filter: PassFilter) : ShuttlePassListAction
    data class QueryChange(val query: String) : ShuttlePassListAction
    data object UploadAll : ShuttlePassListAction
    data object DismissSyncResult : ShuttlePassListAction
    data class OpenPass(val id: String) : ShuttlePassListAction
    data object Back : ShuttlePassListAction
    data object ShowReauth : ShuttlePassListAction
    data class SubmitReauth(val username: String, val password: String) : ShuttlePassListAction
    data object DismissReauth : ShuttlePassListAction
}
