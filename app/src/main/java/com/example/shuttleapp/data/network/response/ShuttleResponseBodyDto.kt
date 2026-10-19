package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName

data class ShuttlePassWithPassengerResponseBodyDto(
    @SerializedName("message")
    val message: String?
)
