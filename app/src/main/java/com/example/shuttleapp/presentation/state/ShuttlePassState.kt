package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.ShuttlePass

data class ShuttlePassState(
    val isLoading: Boolean = false,
    val data: ShuttlePass? = null,
    val error: String? = null
)
