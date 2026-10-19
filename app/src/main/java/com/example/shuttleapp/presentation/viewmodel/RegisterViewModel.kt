package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shuttleapp.domain.model.Registration
import com.example.shuttleapp.domain.repository.AuthRepository
import com.example.shuttleapp.domain.repository.ShuttleProviderRepository
import com.example.shuttleapp.domain.usecase.login.ValidatePasswordInput
import com.example.shuttleapp.domain.usecase.login.ValidateUsernameInput
import com.example.shuttleapp.domain.usecase.register.ValidateEmpNoInput
import com.example.shuttleapp.domain.usecase.register.ValidateFirstNameInput
import com.example.shuttleapp.domain.usecase.register.ValidateLastNameInput
import com.example.shuttleapp.domain.usecase.register.ValidateMiddleNameInput
import com.example.shuttleapp.domain.usecase.register.ValidateProviderInput
import com.example.shuttleapp.domain.usecase.register.ValidateRePasswordInput
import com.example.shuttleapp.presentation.events.RegisterInputEvent
import com.example.shuttleapp.presentation.state.RegisterState
import com.example.shuttleapp.presentation.state.ShuttleProviderState
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
class RegisterViewModel @Inject constructor(
    private val authRepository : AuthRepository,
    private val providerRepository: ShuttleProviderRepository,
    private val validateUsernameInput: ValidateUsernameInput,
    private val validatePasswordInput: ValidatePasswordInput,
    private val validateRePasswordInput: ValidateRePasswordInput,
    private val validateFirstNameInput: ValidateFirstNameInput,
    private val validateMiddleNameInput: ValidateMiddleNameInput,
    private val validateLastNameInput: ValidateLastNameInput,
    private val validateProviderInput: ValidateProviderInput,
    private val validateEmpNoInput: ValidateEmpNoInput

) : ViewModel() {

    //todo dapat list ng id na to for validation, hindi na yung providername itself
    val shuttleProviders: List<String> = listOf(
        "BCTSS", "3R&N", "CHERICH")

    private val _shuttleProviderState = MutableStateFlow((ShuttleProviderState()))
    val shuttleProviderState: StateFlow<ShuttleProviderState> get() = _shuttleProviderState

    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> get() = _registerState

    init{
        getAllShuttleProviders()
    }

    fun getAllShuttleProviders() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = providerRepository.getShuttleProviders()
                response.collectLatest { result ->
                    when(result){
                        is Resource.Error -> {
                            _shuttleProviderState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _shuttleProviderState.update {
                                it.copy(
                                    isLoading = true
                                )
                            }
                        }
                        is Resource.Success -> {
                            result.data?.let {
                                _shuttleProviderState.update {
                                    it.copy(
                                        isLoading = false,
                                        list = result.data
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } catch (ex: HttpException) {
                ex.printStackTrace()
            }
        }
    }
    private fun registerAccount() {
        val state = _registerState.value
        val account = Registration(
            firstName = state.firstName,
            lastName = state.lastName,
            middleName = state.middleName,
            password = state.password,
            shuttleProviderId = state.provider,
            userName = state.userName,
            employeeNumber = state.employeeNumber
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = authRepository.postRegister(account)
                response.collectLatest { result ->
                    when(result) {
                        is Resource.Error -> {
                            _registerState.update {
                                it.copy(
                                    registerErrorMessage = result.message,
                                    isLoading = false,
                                    isSuccessfullyRegistered = false
                                )
                            }
                        }
                        is Resource.Loading -> {
                            _registerState.update {
                                it.copy(
                                    isLoading = true,
                                )
                            }
                        }
                        is Resource.Success -> {
                            _registerState.update {
                                it.copy(
                                    registerErrorMessage = null,
                                    isLoading = false,
                                    isSuccessfullyRegistered = true

                                )
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } catch (ex: HttpException) {
                ex.printStackTrace()
            }
        }
    }

    fun onRegisterInputEvent(event: RegisterInputEvent) {
        when(event) {
            is RegisterInputEvent.FirstnameChanged -> {
                _registerState.update { it.copy(firstName = event.firstname) }
            }
            is RegisterInputEvent.LastnameChanged -> {
                _registerState.update { it.copy(lastName = event.lastname) }
            }
            is RegisterInputEvent.MiddlenameChanged -> {
                _registerState.update { it.copy(middleName = event.middlename) }
            }
            is RegisterInputEvent.PasswordChanged -> {
                _registerState.update { it.copy(password = event.password) }
            }
            is RegisterInputEvent.ProviderChanged -> {
                _registerState.update { it.copy(provider = event.provider) }
            }
            is RegisterInputEvent.EmployeeNumberChanged -> {
                _registerState.update { it.copy(employeeNumber = event.empNumber) }
            }
            is RegisterInputEvent.RePasswordChanged -> {
                _registerState.update { it.copy(repeatPassword = event.rePassword) }
            }
            is RegisterInputEvent.UsernameChanged -> {
                _registerState.update { it.copy(userName = event.username) }
            }
            is RegisterInputEvent.PasswordShown -> {
                _registerState.update { it.copy(isPasswordShown = event.isPasswordShown) }
            }
            is RegisterInputEvent.RePasswordShown -> {
                _registerState.update { it.copy(isRepeatPasswordShown = event.isRePasswordShown) }
            }
            RegisterInputEvent.Submit -> {
                if (submitRegistration()) registerAccount()
            }


        }
    }

    fun submitRegistration() : Boolean {

        val firstnameResult = validateFirstNameInput.execute(_registerState.value.firstName)
        val middlenameResult = validateMiddleNameInput.execute(_registerState.value.middleName)
        val lastnameResult = validateLastNameInput.execute(_registerState.value.lastName)
        //val providerResult = validateProviderInput.execute(_registerState.value.provider, shuttleProviders)
        val empNumberResult = validateEmpNoInput.execute(_registerState.value.employeeNumber)
        val usernameResult = validateUsernameInput.execute(_registerState.value.userName)
        val passwordResult = validatePasswordInput.execute(_registerState.value.password)
        val rePasswordResult = validateRePasswordInput.execute(
            rePassword = _registerState.value.repeatPassword,
            password = _registerState.value.password
        )

        val hasError = listOf(
            firstnameResult,
            middlenameResult,
            lastnameResult,
            //providerResult,
            usernameResult,
            passwordResult,
            rePasswordResult,
            empNumberResult
        ).any { authValidationResult ->
            !authValidationResult.successful
        }

        if(hasError) {
            _registerState.value = _registerState.value.copy(
                firstNameErrorMessage = firstnameResult.errorMessage,
                middleNameErrorMessage = middlenameResult.errorMessage,
                lastNameErrorMessage = lastnameResult.errorMessage,
                //providerErrorMessage = providerResult.errorMessage,
                employeeNumberErrorMessage = empNumberResult.errorMessage,
                userNameErrorMessage = usernameResult.errorMessage,
                passwordErrorMessage = passwordResult.errorMessage,
                repeatPasswordErrorMessage = rePasswordResult.errorMessage,
            )
            return false
        }

        return true

    }

    fun resetFirstNameErrorMessage() {
        _registerState.update { it.copy(firstNameErrorMessage = null) }
    }
    fun resetMiddleNameErrorMessage() {
        _registerState.update { it.copy(middleNameErrorMessage = null) }
    }
    fun resetLastNameErrorMessage() {
        _registerState.update { it.copy(lastNameErrorMessage = null) }
    }
    fun resetProviderErrorMessage() {
        _registerState.update { it.copy(providerErrorMessage = null) }
    }
    fun resetUserNameErrorMessage() {
        _registerState.update { it.copy(userNameErrorMessage = null) }
    }
    fun resetPasswordErrorMessage() {
        _registerState.update { it.copy(passwordErrorMessage = null) }
    }
    fun resetRePasswordErrorMessage() {
        _registerState.update { it.copy(repeatPasswordErrorMessage = null) }
    }
    fun resetEmpNumErrorMessage() {
        _registerState.update { it.copy(employeeNumberErrorMessage = null) }
    }
    fun resetRegisterValidationMessage() {
        _registerState.update {
            it.copy(
                firstNameErrorMessage = null,
                middleNameErrorMessage = null,
                lastNameErrorMessage = null,
                providerErrorMessage = null,
                userNameErrorMessage = null,
                passwordErrorMessage = null,
                repeatPasswordErrorMessage = null,
                registerErrorMessage = null
            )
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState()
    }

}