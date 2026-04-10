plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.common"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
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
    testImplementation(project(":game:logic:impl"))
}
