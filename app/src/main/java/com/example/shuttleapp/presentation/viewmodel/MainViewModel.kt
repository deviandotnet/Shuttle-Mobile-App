package com.example.shuttleapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shuttleapp.domain.model.SessionLoad
import com.example.shuttleapp.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Decides where the app starts: a stored session opens Home directly, online or offline. */
@HiltViewModel
class MainViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : ViewModel() {
    val session: StateFlow<SessionLoad> = sessionRepository.session
}
