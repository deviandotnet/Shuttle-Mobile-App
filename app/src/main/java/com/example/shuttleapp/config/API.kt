package com.example.shuttleapp.config

import com.example.shuttleapp.BuildConfig

object API {
    // Set per build type in app/build.gradle.kts: debug uses http://10.0.2.2:5105 (the emulator's alias
    // for the host machine), release uses the https URL passed as -PshuttleApiUrl.
    const val BASE_URL = BuildConfig.BASE_URL

    const val LOGIN_PATH = "users/login/"
    const val REGISTER_PATH = "users/register/"
}
