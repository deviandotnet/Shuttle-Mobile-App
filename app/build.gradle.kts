plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.kotlin.parcelize) // for serialization

    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val releaseApiUrl: String? = providers.gradleProperty("shuttleApiUrl").orNull
// Debug talks to the PC's backend through `adb reverse` (the adbReverse task below), so the same URL works on the
// emulator and on phones over wireless debugging, whatever LAN IP DHCP gives the PC. Override with -PshuttleDebugApiUrl.
val debugApiUrl: String = providers.gradleProperty("shuttleDebugApiUrl").orNull ?: "http://localhost:5105/api/"
val backendPort = 5105

// A release APK without a real https API URL could never sign in or upload, so refuse to build one.
gradle.taskGraph.whenReady {
    val buildsRelease = allTasks.any { it.project == project && it.name.contains("Release") }
    if (buildsRelease && (releaseApiUrl.isNullOrBlank() || !releaseApiUrl.startsWith("https://"))) {
        throw GradleException("Release builds need -PshuttleApiUrl=https://<host>/api/ (got: ${releaseApiUrl ?: "nothing"})")
    }
}

android {
    namespace = "com.example.shuttleapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.shuttleapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // localhost:5105 on the device is forwarded to the PC's local ASP.NET Core API by adbReverse.
            buildConfigField("String", "BASE_URL", "\"$debugApiUrl\"")
        }
        release {
            // Pass the production API with -PshuttleApiUrl=https://... (or set it in gradle.properties).
            // Missing/non-https values fail any release task (see the taskGraph guard below), never debug builds.
            buildConfigField("String", "BASE_URL", "\"${releaseApiUrl.orEmpty()}\"")
            // The APK is sideloaded (no Play Store ABI splits), so ship only the phone ABIs to keep the bundled
            // ML Kit libraries small: arm64 plus armeabi-v7a for 32-bit / Android Go phones. Debug keeps every
            // ABI so the x86_64 emulator still installs.
            ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Forwards localhost:5105 on every connected device (emulators and wireless-debugging phones) to this PC.
// Runs before each debug build, so pressing Run sets it up; run `./gradlew adbReverse` after reconnecting a device.
// It only warns on failure: a missing adb or no device must never break the build.
val adbReverse = tasks.register("adbReverse") {
    group = "shuttle"
    description = "Runs adb reverse tcp:$backendPort tcp:$backendPort on every connected device."
    val adb = androidComponents.sdkComponents.adb
    outputs.upToDateWhen { false }
    doLast {
        try {
            val adbPath = adb.get().asFile.absolutePath
            fun runAdb(vararg args: String): Pair<Int, String> {
                val process = ProcessBuilder(adbPath, *args).redirectErrorStream(true).start()
                val output = process.inputStream.bufferedReader().readText()
                return process.waitFor() to output.trim()
            }
            val (devicesExit, devicesOutput) = runAdb("devices")
            if (devicesExit != 0) {
                logger.warn("adbReverse: `adb devices` failed: $devicesOutput")
                return@doLast
            }
            val serials = devicesOutput.lines()
                .drop(1)
                .map { it.trim().split(Regex("\\s+")) }
                .filter { it.size >= 2 && it[1] == "device" }
                .map { it[0] }
            if (serials.isEmpty()) {
                logger.warn("adbReverse: no device connected, skipped. Run `./gradlew adbReverse` once one is.")
                return@doLast
            }
            serials.forEach { serial ->
                val (exit, output) = runAdb("-s", serial, "reverse", "tcp:$backendPort", "tcp:$backendPort")
                if (exit == 0) {
                    logger.lifecycle("adbReverse: $serial localhost:$backendPort -> PC:$backendPort")
                } else {
                    logger.warn("adbReverse: $serial failed: $output")
                }
            }
        } catch (e: Exception) {
            logger.warn("adbReverse: skipped, could not run adb: ${e.message}")
        }
    }
}

tasks.named { it == "preDebugBuild" }.configureEach { dependsOn(adbReverse) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    //RETROFIT
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    //encrypted session/token storage
    implementation(libs.androidx.security.crypto)

    //encrypted Room database (SQLCipher)
    implementation(libs.sqlcipher.android)

    //COIL
    implementation("io.coil-kt:coil-compose:2.4.0")

    //navigation bars
    implementation("androidx.navigation:navigation-compose:2.9.0")

    //compose viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    //compose and navigation (UI)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.compose.ui:ui:1.8.2")
    implementation("androidx.compose.material:material:1.8.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.2")

    //room db
    implementation("androidx.room:room-ktx:2.7.1") //room coroutine support
    implementation("androidx.room:room-runtime:2.7.1") //enable room in your project
    implementation("androidx.room:room-paging:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    //QR decoding: ML Kit's bundled model ships inside the APK, so it works without Google Play services
    //and offline from the first launch (the app is sideloaded, not installed from the Play Store)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    //CameraX
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation("androidx.camera:camera-mlkit-vision:1.5.1")
    implementation(libs.androidx.concurrent.futures) // align with androidx.test, see libs.versions.toml

    //icons extended
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    //Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    //SYSTEM UI CONTROLLER - change system navcolors
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")

    //for memory leak inspection
    //debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

    //Data Store
    //implementation("androidx.datastore:datastore-preferences-core:1.1.7")

}