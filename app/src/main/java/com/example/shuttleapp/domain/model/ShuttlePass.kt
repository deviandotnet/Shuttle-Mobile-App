package com.example.shuttleapp.domain.model

import java.util.UUID

data class ShuttlePass(
    val id: String = UUID.randomUUID().toString(),
    val routeId: String = "",
    val provider: String = "",
    val driver: String = "",
    val plateNumber: String = "",
    val date: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val tripType: String = "",
    val departure: String = "",
    val arrival: String = "",
    val isSynced: Boolean = false,
    val isLateShuttle: Boolean = false,
    val isDraft: Boolean = true
)
