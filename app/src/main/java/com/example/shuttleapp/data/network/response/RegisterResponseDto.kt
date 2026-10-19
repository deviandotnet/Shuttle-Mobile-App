package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class RegisterResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("title")
    val title: String
)
