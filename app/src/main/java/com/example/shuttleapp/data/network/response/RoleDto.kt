package com.example.shuttleapp.data.network.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RoleDto(
    @SerializedName("roleid")
    val roleid: Int?,
    @SerializedName("rolename")
    val rolename: String?
)
