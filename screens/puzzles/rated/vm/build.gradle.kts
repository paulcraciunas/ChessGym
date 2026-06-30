plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.puzzles.rated.vm"
    di = true
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":global:navigation"))
    implementation(project(":global:qualifiers"))
    implementation(project(":global:sounds"))
    implementation(project(":screens:data"))
    implementation(project(":settings:application:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.public.timber)

    testImplementation(testFixtures(project(":settings:application:api")))
    testImplementation(testFixtures(project(":domain:api")))
    testImplementation(testFixtures(project(":user:api")))
    testImplementation(project(":domain:impl"))
    testImplementation(project(":game:logic:impl"))
}
