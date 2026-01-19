plugins {
    id("conventions.android.app")
}

chessGymApp {
    namespace = "com.paulcraciunas.chessgym"
    applicationId = "com.paulcraciunas.chessgym"
    proguardFile("proguard-rules.pro")
}

dependencies {
    implementation(project(":game:puzzles:di"))
    implementation(project(":game:logic:di"))
    implementation(project(":game:serializer:di"))
    implementation(project(":domain:di"))
    implementation(project(":global:device:di"))
    implementation(project(":global:notifications"))
    implementation(project(":global:resources"))
    implementation(project(":settings:application:impl"))
    implementation(project(":screens:common"))
    implementation(project(":screens:loading:ui"))
    implementation(project(":screens:home:ui"))
    implementation(project(":screens:puzzles:dashboard:ui"))
    implementation(project(":screens:puzzles:rated:ui"))
    implementation(project(":user:di"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui.graphics)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Edge-to-edge
    implementation(libs.androidx.activity.ktx)

    // Work Manager Integration
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)
}
