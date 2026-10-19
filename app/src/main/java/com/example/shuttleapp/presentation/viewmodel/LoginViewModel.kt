package com.example.shuttleapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.LoginCredentials
import com.example.shuttleapp.domain.repository.AuthRepository
import com.example.shuttleapp.domain.usecase.login.ValidateLoginInput
import com.example.shuttleapp.presentation.events.LoginInputEvent
import com.example.shuttleapp.presentation.state.LoginState
import com.example.shuttleapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
// A successful login starts the persistent session (SessionRepository); the app then stays signed in
// offline until an explicit logout, so there is no separate "Remember me" step.
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateLoginInput: ValidateLoginInput
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> get() = _loginState

    private fun login() {
        val credentials = LoginCredentials(username = _loginState.value.username, password = _loginState.value.password)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = authRepository.postLogin(credentials)
                response.collectLatest { result ->
                    when(result) {
                        is Resource.Error -> {
                            _loginState.update { it.copy(
                                isLoading = false,
                                loginErrorMessage = result.message,
                                isSuccessfullyLoggedIn = false
                            ) }
                        }
                        is Resource.Loading -> {
                            _loginState.update { it.copy(
                                isLoading = true
                            ) }
                        }
                        is Resource.Success -> {
                            _loginState.update { it.copy(
                                userData = result.data,
                                isLoading = false,
                                isSuccessfullyLoggedIn = true,
                                loginErrorMessage = null
                            ) }
                        }
                    }

                }
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
            catch (ex: HttpException) {
                ex.printStackTrace()
            }
        }
    }

    //use cases and validations
//    private val validateUsernameInput: ValidateUsernameInput = ValidateUsernameInput()
//    private val validatePasswordInput: ValidatePasswordInput = ValidatePasswordInput()

    fun onLoginInputEvent(event : LoginInputEvent) {
        when(event) {
            is LoginInputEvent.UsernameChanged -> {
                _loginState.value = _loginState.value.copy(username = event.username)
            }
            is LoginInputEvent.PasswordChanged -> {
                _loginState.value = _loginState.value.copy(password = event.password)
            }
            is LoginInputEvent.PasswordShown -> {
                _loginState.value = _loginState.value.copy(isPasswordShown = event.isShown)
            }
            LoginInputEvent.Submit -> {
                if (submitLogin()) login()
            }


        }
    }

    fun submitLogin() : Boolean {
//        var usernameResult = validateUsernameInput.execute(_loginState.value.username)
//        var passwordResult = validatePasswordInput.execute(_loginState.value.password)
//
        val loginResult = validateLoginInput.execute(_loginState.value.username, _loginState.value.password)

        val hasError = listOf(
            loginResult

        ).any{ authValidationResult ->
            !authValidationResult.successful
        }

        if(hasError) {
            _loginState.value = _loginState.value.copy(
//                usernameErrorMessage = usernameResult.errorMessage,
//                passwordErrorMessage = passwordResult.errorMessage,
                loginErrorMessage = loginResult.errorMessage
            )
            return false
        }

        return true
    }

    fun resetLoginState() {
        _loginState.value = LoginState()
    }
    fun resetLoginValidationMessage() {
        _loginState.update { it.copy(loginErrorMessage = null) }


    }

}