package com.example.shuttleapp.presentation.state

import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger

data class ShuttlePassListLegacyState(
    val loading: Boolean = false,
    val list: List<ShuttlePassWithPassenger> = emptyList(),
    /** [list] formatted for display, newest first. */
    val items: List<ShuttlePassItemUi> = emptyList(),
    val error: String? = null
)
