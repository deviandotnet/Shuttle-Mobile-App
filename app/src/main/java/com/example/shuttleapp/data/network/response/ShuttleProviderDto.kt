package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class ShuttleProviderDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("isActive")
    val isActive: Boolean?, //todo delete this and all of the things that reference this
    @SerializedName("providerName")
    val providerName: String?
)