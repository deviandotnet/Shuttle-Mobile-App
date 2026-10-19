package com.example.shuttleapp.domain.usecase.logout

import com.example.shuttleapp.domain.repository.SessionRepository
import com.example.shuttleapp.domain.repository.UserDataRepository
import javax.inject.Inject

/**
 * Explicit logout: ends the session and clears the cached profile.
 * Shuttle passes are deliberately kept, so unsynced ones upload after the driver signs in again.
 */
class LogoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke() {
        sessionRepository.end()
        userDataRepository.clearUserData()
    }
}
