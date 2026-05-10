plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.signin.vm"
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":user:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.public.timber)

    testImplementation(testFixtures(project(":user:api")))
    testImplementation(project(":domain:impl"))
    testImplementation(testFixtures(project(":domain:api")))
}
