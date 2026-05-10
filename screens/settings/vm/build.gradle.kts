plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.settings.vm"
    di = true
}

dependencies {
    implementation(project(":settings:application:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":settings:application:api")))
}
