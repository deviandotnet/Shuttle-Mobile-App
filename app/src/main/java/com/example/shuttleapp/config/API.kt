package com.example.shuttleapp.config

import com.example.shuttleapp.BuildConfig

object API {
    // Set per build type in app/build.gradle.kts: debug uses http://localhost:5105 (tunnelled to the PC by
    // the adbReverse Gradle task, on emulators and phones alike), release uses the https URL passed as -PshuttleApiUrl.
    const val BASE_URL = BuildConfig.BASE_URL

    const val LOGIN_PATH = "users/login/"
    const val REGISTER_PATH = "users/register/"
}
