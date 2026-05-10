plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.achievements.vm"
    di = true
}

dependencies {
    api(project(":domain:api"))
    implementation(project(":user:api"))

    implementation(libs.public.timber)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
