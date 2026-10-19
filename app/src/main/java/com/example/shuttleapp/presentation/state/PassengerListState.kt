package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.PassengerQR

data class PassengerListState(
    val loading: Boolean = false,
    val list: List<PassengerQR> = emptyList(),
    val error: String? = null
)
