package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class LoginResponseBodyDto(
    val message: String?,
    val userData: UserDataDto?,
    // Not sent by the backend yet (see docs/backend-auth-contract.md); null until it is.
    @SerializedName("token") val token: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null
)
