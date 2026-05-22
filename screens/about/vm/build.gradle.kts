plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.about.vm"
    compose = true
    di = true
}

dependencies {
    implementation(project(":settings:application:api"))
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":settings:application:api")))
}
