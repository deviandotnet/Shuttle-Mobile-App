package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class ShuttlePassDto(

    @SerializedName("arrivaltime")
    val arrivaltime: String?,
    @SerializedName("datecreated")
    val datecreated: String?,
    @SerializedName("departuretime")
    val departuretime: String?,
    @SerializedName("driverid")
    val driverid: String?,
    @SerializedName("islateshuttle")
    val islateshuttle: Boolean?,
)
