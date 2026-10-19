package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class ShuttleDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("plateNumber")
    val plateNumber: String?,
    @SerializedName("providerId")
    val providerId: String?
)