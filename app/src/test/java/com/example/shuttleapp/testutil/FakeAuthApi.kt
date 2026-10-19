package com.example.shuttleapp.testutil

import com.example.shuttleapp.data.network.AuthApi
import com.example.shuttleapp.data.network.request.LoginRequestBodyDto
import com.example.shuttleapp.data.network.request.RegisterDto
import com.example.shuttleapp.data.network.response.LoginResponseBodyDto
import com.example.shuttleapp.data.network.response.RegisterResponse
import com.example.shuttleapp.data.network.response.UserDataDto
import retrofit2.Response
import java.io.IOException

class FakeAuthApi : AuthApi {

    /** Returned from postLogin; set to null to simulate no network (IOException). */
    var loginResponse: Response<LoginResponseBodyDto>? = success(accountId = "driver-1")
    val loginRequests = mutableListOf<LoginRequestBodyDto>()

    override suspend fun postLogin(credentialDto: LoginRequestBodyDto): Response<LoginResponseBodyDto> {
        loginRequests += credentialDto
        return loginResponse ?: throw IOException("offline")
    }

    override suspend fun postRegister(registerDto: RegisterDto): Response<RegisterResponse> =
        throw UnsupportedOperationException()

    companion object {
        fun success(accountId: String, token: String? = null, expiresAt: String? = null): Response<LoginResponseBodyDto> =
            Response.success(
                LoginResponseBodyDto(
                    message = AuthApi.LOGIN_SUCCESS_MESSAGE,
                    userData = UserDataDto(accountId, "E-1", "Juan", "Dela Cruz", "", null),
                    token = token,
                    expiresAt = expiresAt
                )
            )
    }
}
