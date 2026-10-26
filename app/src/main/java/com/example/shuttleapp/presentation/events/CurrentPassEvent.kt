package com.example.shuttleapp.presentation.events

sealed interface CurrentPassEvent {
    /** [message] is shown as a toast on the screen we return to. */
    data class NavigateBack(val message: String? = null) : CurrentPassEvent
    data class NavigateToScanner(val passId: String) : CurrentPassEvent
    data class NavigateToEditPass(val passId: String) : CurrentPassEvent
    data class ShowMessage(val message: String) : CurrentPassEvent
}
