plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.boardvis.pieces.vm"
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":user:api"))
    implementation(project(":screens:common"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":game:logic:impl"))
    testImplementation(testFixtures(project(":user:api")))
    testImplementation(testFixtures(project(":domain:api")))
}
