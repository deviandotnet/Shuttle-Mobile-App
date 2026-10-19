package com.example.shuttleapp.domain.model

import java.util.UUID

data class PassengerQR(
    val id: String = UUID.randomUUID().toString(),
    val shuttlePassId: String = "",
    val scannedQR: String = "",
    val timeIn: String = "",
    val isDraft: Boolean = true
)
