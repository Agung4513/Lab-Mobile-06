plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.habittracker"
    compileSdk = 36 // DIPERBAIKI: Naikkan ke 36 agar kompatibel dengan library AndroidX terbaru

    defaultConfig {
        applicationId = "com.example.habittracker"
        minSdk = 24
        targetSdk = 35 // Tetap gunakan targetSdk 35 untuk keamanan runtime aplikasi
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Networking & JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)

    // Image Loading
    implementation(libs.glide)

    // PERBAIKAN 1: Menggunakan tanda kurung dan petik ganda (Kotlin DSL standard)
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // PERBAIKAN 2: Memasukkan kembali library testing ke dalam blok dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
} // Akhir blok dependencies yang benar