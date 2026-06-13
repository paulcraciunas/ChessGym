plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.settings.vm"
    di = true
    compose = true
}

dependencies {
    implementation(project(":settings:application:api"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":settings:application:api")))
}
