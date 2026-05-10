plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.loading.vm"
    di = true
}

dependencies {
    implementation(project(":game:puzzles:api"))
    implementation(project(":global:device:api"))
    implementation(project(":settings:application:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.public.timber)

    testImplementation(testFixtures(project(":global:device:api")))
    testImplementation(testFixtures(project(":settings:application:api")))
}
