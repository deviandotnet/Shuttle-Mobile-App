package com.example.shuttleapp.presentation.events

sealed class ShuttlePassUiEvent {
    object NavigateUp : ShuttlePassUiEvent()
    data class ShowToast(val message: String) : ShuttlePassUiEvent()
}
