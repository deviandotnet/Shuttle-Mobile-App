package com.example.shuttleapp.di

import com.example.shuttleapp.data.repositoryImpl.AuthRepositoryImpl
import com.example.shuttleapp.data.repositoryImpl.LoggedUserRepositoryImpl
import com.example.shuttleapp.data.repositoryImpl.RoleRepositoryImpl
import com.example.shuttleapp.data.repositoryImpl.ShuttleProviderRepositoryImpl
import com.example.shuttleapp.data.repositoryImpl.ShuttleRepositoryImpl
import com.example.shuttleapp.data.repositoryImpl.UserDataRepositoryImpl
import com.example.shuttleapp.domain.repository.AuthRepository
import com.example.shuttleapp.domain.repository.RememberedUserRepository
import com.example.shuttleapp.domain.repository.RoleRepository
import com.example.shuttleapp.domain.repository.ShuttleProviderRepository
import com.example.shuttleapp.domain.repository.ShuttleRepository
import com.example.shuttleapp.domain.repository.UserDataRepository
import com.example.shuttleapp.data.repositoryImpl.PassengerQRRepositoryImpl
import com.example.shuttleapp.data.repositoryImpl.RoutesRepositoryImpl
import com.example.shuttleapp.data.repositoryImpl.ShuttlePassRepositoryImpl
import com.example.shuttleapp.domain.repository.PassengerQRRepository
import com.example.shuttleapp.domain.repository.RoutesRepository
import com.example.shuttleapp.domain.repository.ShuttlePassRepository
import com.example.shuttleapp.data.local.EncryptedSessionStore
import com.example.shuttleapp.data.local.SessionStore
import com.example.shuttleapp.data.network.ConnectivityNetworkMonitor
import com.example.shuttleapp.data.repositoryImpl.SessionRepositoryImpl
import com.example.shuttleapp.domain.repository.NetworkMonitor
import com.example.shuttleapp.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {


    @Binds
    @Singleton
    abstract fun bindPassengerQRRepository(
        passengerQRRepositoryImpl: PassengerQRRepositoryImpl
    ) : PassengerQRRepository

    @Binds
    @Singleton
    abstract fun bindRoutesRepository(
        routesRepositoryImpl: RoutesRepositoryImpl
    ) : RoutesRepository


    @Binds
    @Singleton
    abstract fun bindShuttlePassRepository(
        shuttlePassRepositoryImpl : ShuttlePassRepositoryImpl
    ) : ShuttlePassRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserDataRepositoryImpl
    ) : UserDataRepository

    @Binds
    @Singleton
    abstract fun bindRoleRepository(
        roleRepositoryImpl: RoleRepositoryImpl
    ) : RoleRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ) : AuthRepository

    @Binds
    @Singleton
    abstract fun bindShuttleProviderRepository(
        shuttleProviderRepositoryImpl: ShuttleProviderRepositoryImpl
    ) : ShuttleProviderRepository

    @Binds
    @Singleton
    abstract fun bindShuttleRepository(
        shuttleRepositoryImpl: ShuttleRepositoryImpl
    ) : ShuttleRepository

    @Binds
    @Singleton
    abstract fun bindLoggedUserRepository(
        loggedUserRepositoryImpl: LoggedUserRepositoryImpl
    ) : RememberedUserRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ) : SessionRepository

    @Binds
    @Singleton
    abstract fun bindSessionStore(
        encryptedSessionStore: EncryptedSessionStore
    ) : SessionStore

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        connectivityNetworkMonitor: ConnectivityNetworkMonitor
    ) : NetworkMonitor
}