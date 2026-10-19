package com.example.shuttleapp.data.mappers

import com.example.shuttleapp.data.local.ShuttlePassWithPassengerEntity
import com.example.shuttleapp.data.network.response.ShuttlePassWithPassengerDto
import com.example.shuttleapp.domain.model.ShuttlePassWithPassenger

fun ShuttlePassWithPassengerEntity.toDto() : ShuttlePassWithPassengerDto {

    return ShuttlePassWithPassengerDto(
        id = shuttlePass.id,
        shuttleId = shuttlePass.plateNumber,
        routeId = shuttlePass.routeId,
        tripType = shuttlePass.tripType,
        arrivalTime = shuttlePass.arrival,
        dateCreated = shuttlePass.date,
        departureTime = shuttlePass.departure,
        driverId = shuttlePass.driver,
        isLateShuttle = shuttlePass.isLateShuttle,
        passengers = passengersQR.map { it.toDto() }

    )
}

fun ShuttlePassWithPassengerEntity.toDomain(): ShuttlePassWithPassenger {
    return ShuttlePassWithPassenger(
        shuttlePass = shuttlePass.toDomain(),
        passengersQR = passengersQR.map { it.toDomain() }
    )
}
