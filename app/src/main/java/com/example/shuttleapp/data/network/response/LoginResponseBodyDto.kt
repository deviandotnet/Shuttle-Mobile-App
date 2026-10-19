package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class LoginResponseBodyDto(
    @SerializedName("message")
    val message: String?,
    @SerializedName("userData")
    val userData: UserDataDto?
)
