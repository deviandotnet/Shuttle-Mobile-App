plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-parcelize") // for serialization

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
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
        release {
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

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
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

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

    //import qr zxing library
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")
    implementation("com.google.zxing:core:3.5.3")

    //other dependency for barcode scanning
    //implementation("com.google.android.gms:play-services-code-scanner:16.1.0") //optional for zxing

    //CameraX
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation(libs.androidx.concurrent.futures) // align with androidx.test, see libs.versions.toml

    //icons extended
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    //SYSTEM UI CONTROLLER - change system navcolors
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")

    //for memory leak inspection
    //debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

    //Data Store
    //implementation("androidx.datastore:datastore-preferences-core:1.1.7")

}