plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.boardvis.pieces.vm"
    di = true
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":global:qualifiers"))
    implementation(project(":global:sounds"))
    implementation(project(":user:api"))
    implementation(project(":screens:data"))
    implementation(project(":settings:application:api"))

    implementation(libs.public.timber)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":game:logic:impl"))
    testImplementation(project(":domain:impl"))
    testImplementation(testFixtures(project(":settings:application:api")))
    testImplementation(testFixtures(project(":domain:api")))
    testImplementation(testFixtures(project(":user:api")))
}
