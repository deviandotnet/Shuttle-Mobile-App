package com.example.shuttleapp.domain.model

data class LoginCredentials(
    val username: String,
    val password: String
)

data class Registration(
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val password: String,
    val shuttleProviderId: String,
    val userName: String,
    val employeeNumber: String
)
