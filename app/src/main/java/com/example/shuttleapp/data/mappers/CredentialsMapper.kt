package com.example.shuttleapp.data.mappers

import com.example.shuttleapp.data.network.request.LoginRequestBodyDto
import com.example.shuttleapp.data.network.request.RegisterDto
import com.example.shuttleapp.domain.model.LoginCredentials
import com.example.shuttleapp.domain.model.Registration

fun LoginCredentials.toDto(): LoginRequestBodyDto = LoginRequestBodyDto(username = username, password = password)

fun Registration.toDto(): RegisterDto = RegisterDto(
    firstName = firstName,
    lastName = lastName,
    middleName = middleName,
    password = password,
    shuttleProviderId = shuttleProviderId,
    userName = userName,
    employeeNumber = employeeNumber
)
