plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.boardvis.dashboard.vm"
    di = true
}

dependencies {
    implementation(project(":user:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":user:api")))
}
