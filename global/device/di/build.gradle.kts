plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.device.di"
    di = true
}

dependencies {
    api(project(":global:device:api"))
    implementation(project(":global:device:impl"))
}
