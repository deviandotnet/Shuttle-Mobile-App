package com.example.shuttleapp.data.mappers

import com.example.shuttleapp.data.local.ShuttleProviderEntity
import com.example.shuttleapp.data.network.response.ShuttleProviderDto
import com.example.shuttleapp.domain.model.ShuttleProvider

fun ShuttleProviderDto.toShuttleProviderEntity() : ShuttleProviderEntity {
    return ShuttleProviderEntity(
        shuttleProviderId = id ?: "",
        providerName = providerName ?: "",
        isActive = isActive ?: false
    )
}

// Only active providers are cached locally (they come from login / the provider list).
fun ShuttleProvider.toEntity() : ShuttleProviderEntity {
    return ShuttleProviderEntity(
        shuttleProviderId = shuttleProviderId,
        providerName = providerName,
        isActive = true
    )
}

fun ShuttleProviderEntity.toShuttleProvider() : ShuttleProvider {
    return ShuttleProvider(
        shuttleProviderId = shuttleProviderId,
        providerName = providerName
    )
}