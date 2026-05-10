plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.device.impl"
}

dependencies {
    implementation(project(":global:device:api"))
    
    implementation(libs.hilt.android)
    implementation(libs.public.timber)
}
