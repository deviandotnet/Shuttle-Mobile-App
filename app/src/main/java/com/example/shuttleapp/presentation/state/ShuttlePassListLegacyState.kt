package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger

data class ShuttlePassListLegacyState(
    val loading: Boolean = false,
    val list: List<ShuttlePassWithPassenger> = emptyList(),
    val error: String? = null
)
