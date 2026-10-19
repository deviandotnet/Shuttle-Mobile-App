package com.example.shuttleapp.di

import com.example.shuttleapp.domain.repository.UserDataRepository
import com.example.shuttleapp.domain.usecase.login.ValidateLoginInput
import com.example.shuttleapp.domain.usecase.login.ValidatePasswordInput
import com.example.shuttleapp.domain.usecase.login.ValidateUsernameInput
import com.example.shuttleapp.domain.usecase.logout.LogoutUseCase
import com.example.shuttleapp.domain.repository.SessionRepository
import com.example.shuttleapp.domain.usecase.manualaddpassenger.ValidateQRCodeInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateDriverInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidatePlateNumberInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateRouteInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateShuttleProviderInput
import com.example.shuttleapp.domain.usecase.manualinputshuttlepass.ValidateTripTypeInput
import com.example.shuttleapp.domain.usecase.register.ValidateEmpNoInput
import com.example.shuttleapp.domain.usecase.register.ValidateFirstNameInput
import com.example.shuttleapp.domain.usecase.register.ValidateLastNameInput
import com.example.shuttleapp.domain.usecase.register.ValidateMiddleNameInput
import com.example.shuttleapp.domain.usecase.register.ValidateProviderInput
import com.example.shuttleapp.domain.usecase.register.ValidateRePasswordInput
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    //use cases

    @Provides
    fun provideValidateLoginInput() : ValidateLoginInput {
        return ValidateLoginInput()
    }

    @Provides
    fun provideValidatePasswordInput() : ValidatePasswordInput {
        return ValidatePasswordInput()
    }

    @Provides
    fun provideValidateUsernameInput() : ValidateUsernameInput {
        return ValidateUsernameInput()
    }

    @Provides
    fun provideLogoutUseCase(
        sessionRepository: SessionRepository,
        userDataRepository: UserDataRepository
    ) : LogoutUseCase {
        return LogoutUseCase(
            sessionRepository = sessionRepository,
            userDataRepository = userDataRepository
        )
    }

    @Provides
    fun provideValidateFirstNameInput() : ValidateFirstNameInput {
        return ValidateFirstNameInput()
    }

    @Provides
    fun provideValidateMiddleNameInput() : ValidateMiddleNameInput {
        return ValidateMiddleNameInput()
    }

    @Provides
    fun provideValidateLastNameInput() : ValidateLastNameInput {
        return ValidateLastNameInput()
    }

    @Provides
    fun provideProviderInput() : ValidateProviderInput {
        return ValidateProviderInput()
    }

    @Provides
    fun provideValidateRePasswordInput() : ValidateRePasswordInput {
        return ValidateRePasswordInput()
    }

    @Provides
    fun provideValidateEmpNumberInput() : ValidateEmpNoInput {
        return ValidateEmpNoInput()
    }

    @Provides
    fun provideValidateQRCodeInput() : ValidateQRCodeInput {
        return ValidateQRCodeInput()
    }

    @Provides
    fun provideValidateDriverInput() : ValidateDriverInput {
        return ValidateDriverInput()
    }

    @Provides
    fun provideValidatePlateNumberInput() : ValidatePlateNumberInput {
        return ValidatePlateNumberInput()
    }

    @Provides
    fun provideValidateRouteInput() : ValidateRouteInput {
        return ValidateRouteInput()
    }

    @Provides
    fun provideValidateShuttleProviderInput() : ValidateShuttleProviderInput {
        return ValidateShuttleProviderInput()
    }

    @Provides
    fun provideValidateTripTypeInput() : ValidateTripTypeInput {
        return ValidateTripTypeInput()
    }

}