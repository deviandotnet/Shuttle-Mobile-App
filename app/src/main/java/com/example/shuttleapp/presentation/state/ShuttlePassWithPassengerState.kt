package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger

data class ShuttlePassWithPassengerState(
    val isLoading: Boolean = false,
    val data: ShuttlePassWithPassenger? = null,
    val error: String? = null
)