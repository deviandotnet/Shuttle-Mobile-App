package com.example.shuttleapp.presentation.events

sealed class LoginInputEvent {

    data class UsernameChanged(val username: String) : LoginInputEvent()
    data class PasswordChanged(val password: String) : LoginInputEvent()
    data class PasswordShown(val isShown : Boolean) : LoginInputEvent()
    data object Submit : LoginInputEvent()
}