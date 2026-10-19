package com.example.shuttleapp.data.network

import com.example.shuttleapp.config.API
import com.example.shuttleapp.data.network.request.LoginRequestBodyDto
import com.example.shuttleapp.data.network.request.RegisterDto
import com.example.shuttleapp.data.network.response.LoginResponseBodyDto
import com.example.shuttleapp.data.network.response.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    companion object {
        /** The backend reports a successful login with this exact message. */
        const val LOGIN_SUCCESS_MESSAGE = "login successful"
    }

    //@POST("auth/login/")
    @POST(API.LOGIN_PATH)
    suspend fun postLogin(
        @Body
        credentialDto: LoginRequestBodyDto

    ) : Response<LoginResponseBodyDto>

    //@POST("auth/register/")
    @POST(API.REGISTER_PATH)
    suspend fun postRegister(
        @Body
        registerDto: RegisterDto
    ) : Response<RegisterResponse>

//    @PUT("users/forgotpassword/")
//    suspend fun forgotPassword(
//        @Body
//        id: String,
//        userData : UserDataDto
//    )


}