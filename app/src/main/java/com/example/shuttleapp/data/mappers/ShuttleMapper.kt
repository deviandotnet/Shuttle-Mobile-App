package com.example.shuttleapp.data.mappers

import com.example.shuttleapp.data.local.ShuttleEntity
import com.example.shuttleapp.domain.model.Shuttle
import com.example.shuttleapp.data.network.response.ShuttleDto


fun ShuttleDto.toShuttleEntity(): ShuttleEntity {
    return ShuttleEntity(
        shuttleId = id ?: "",
        plateNumber = plateNumber ?: "",
        shuttleProviderId = providerId ?: ""
    )
}

fun Shuttle.toEntity(): ShuttleEntity {
    return ShuttleEntity(
        shuttleId = shuttleId,
        plateNumber = plateNumber,
        shuttleProviderId = shuttleProviderId
    )
}

fun ShuttleEntity.toShuttle() : Shuttle {
    return Shuttle(
        shuttleId = shuttleId,
        plateNumber = plateNumber,
        shuttleProviderId = shuttleProviderId
    )

}