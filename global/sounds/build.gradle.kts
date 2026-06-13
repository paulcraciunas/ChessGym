plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.sounds"
    di = true
}

dependencies {
    implementation(project(":global:qualifiers"))
    implementation(project(":global:resources"))

    implementation(libs.public.timber)
}
