package com.example.shuttleapp.presentation.events

sealed interface ShuttlePassListEvent {
    data class NavigateToPass(val id: String) : ShuttlePassListEvent
    data object NavigateBack : ShuttlePassListEvent
}
