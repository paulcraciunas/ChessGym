plugins {
    id("conventions.android.library")
}

android {
    namespace = "com.paulcraciunas.global.billing"
}

dependencies {
    implementation(project(":user:api"))
    implementation(project(":global:qualifiers"))

    implementation(libs.google.billing)
    implementation(libs.hilt.android)
    implementation(libs.public.timber)
    implementation(libs.kotlinx.coroutines.android)
}
