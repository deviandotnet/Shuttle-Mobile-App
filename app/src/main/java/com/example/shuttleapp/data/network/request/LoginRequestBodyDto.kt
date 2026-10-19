package com.example.shuttleapp.data.network.request

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestBodyDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)