package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PassengerDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("shuttlePassId")
    val shuttlePassId: String?,
    @SerializedName("scannedQr")
    val scannedQr: String?,
    @SerializedName("timeIn")
    val timeIn: String?,

    )
