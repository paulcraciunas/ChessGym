plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.billing"
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":user:api"))
    implementation(project(":global:qualifiers"))

    implementation(libs.google.billing)
    implementation(libs.hilt.android)
    implementation(libs.public.timber)
    implementation(libs.kotlinx.coroutines.android)
}
