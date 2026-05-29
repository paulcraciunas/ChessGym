plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.boardvis.squares.vm"
    di = true
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":user:api"))
    implementation(project(":screens:common"))
    implementation(project(":screens:data"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":user:api")))
    testImplementation(testFixtures(project(":domain:api")))
}
