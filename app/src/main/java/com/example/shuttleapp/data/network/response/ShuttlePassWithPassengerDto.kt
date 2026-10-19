package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ShuttlePassWithPassengerDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("driverId")
    val driverId: String,
    @SerializedName("shuttleId")
    val shuttleId: String,
    @SerializedName("routeId")
    val routeId: String,
    @SerializedName("dateCreated")
    val dateCreated: String,
    @SerializedName("tripType")
    val tripType: String,
    @SerializedName("arrivalTime")
    val arrivalTime: String,
    @SerializedName("departureTime")
    val departureTime: String,
    @SerializedName("isLateShuttle")
    val isLateShuttle: Boolean,

    @SerializedName("passengers")
    val passengers: List<PassengerDto>,

    )
