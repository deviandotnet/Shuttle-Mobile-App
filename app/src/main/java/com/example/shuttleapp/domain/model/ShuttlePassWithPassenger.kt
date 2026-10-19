package com.example.shuttleapp.domain.model

data class ShuttlePassWithPassenger(
    val shuttlePass: ShuttlePass,
    val passengersQR: List<PassengerQR>
)
