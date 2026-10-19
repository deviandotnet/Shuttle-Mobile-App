package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.UserData

data class LoginState(
    val username: String = "",
    val usernameErrorMessage: String? = null,
    val password: String = "",
    val passwordErrorMessage: String? = null,
    val isPasswordShown: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccessfullyLoggedIn: Boolean = false,
    var loginErrorMessage: String? = null,
    val userData: UserData? = null
)

