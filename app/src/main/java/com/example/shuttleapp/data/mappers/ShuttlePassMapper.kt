package com.example.shuttleapp.data.mappers

import com.example.shuttleapp.data.local.ShuttlePassEntity
import com.example.shuttleapp.domain.model.ShuttlePass

fun ShuttlePassEntity.toDomain(): ShuttlePass {
    return ShuttlePass(
        id = id,
        routeId = routeId,
        provider = provider,
        driver = driver,
        plateNumber = plateNumber,
        date = date,
        dateCreated = dateCreated,
        tripType = tripType,
        departure = departure,
        arrival = arrival,
        isSynced = isSynced,
        isLateShuttle = isLateShuttle,
        isDraft = isDraft
    )
}

fun ShuttlePass.toEntity(): ShuttlePassEntity {
    return ShuttlePassEntity(
        id = id,
        routeId = routeId,
        provider = provider,
        driver = driver,
        plateNumber = plateNumber,
        date = date,
        dateCreated = dateCreated,
        tripType = tripType,
        departure = departure,
        arrival = arrival,
        isSynced = isSynced,
        isLateShuttle = isLateShuttle,
        isDraft = isDraft
    )
}
