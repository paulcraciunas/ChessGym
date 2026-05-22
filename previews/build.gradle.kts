plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.paulcraciunas.previews"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.paulcraciunas.previews"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":screens:common"))
    implementation(project(":screens:about:ui"))
    implementation(project(":screens:achievements:ui"))
    implementation(project(":screens:loading:ui"))
    implementation(project(":screens:puzzles:rush:ui"))
    implementation(project(":screens:puzzles:streak:ui"))
    implementation(project(":screens:puzzles:failed:ui"))
    implementation(project(":screens:tools:importgame:ui"))
    implementation(project(":global:resources"))
    implementation(project(":game:logic:api"))

    val bom = platform(libs.androidx.compose.bom)
    implementation(bom)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}
