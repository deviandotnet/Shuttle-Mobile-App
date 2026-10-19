package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class UserDataDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("employeeNumber")
    val employeeNumber: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("middleName")
    val middleName: String?,
    @SerializedName("shuttleProvider")
    val shuttleProvider: ShuttleProviderDto?
)
