plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.clock.vm"
    di = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":domain:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":domain:api")))
}
