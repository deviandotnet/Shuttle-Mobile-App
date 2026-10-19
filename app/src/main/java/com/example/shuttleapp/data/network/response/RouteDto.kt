package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("code")
    val code: String?,
    @SerializedName("routeName")
    val routeName: String?
)
