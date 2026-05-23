plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.common"
    compose = true
}

android {
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":game:logic:api"))
    implementation(project(":domain:api"))
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(project(":game:logic:impl"))
}
