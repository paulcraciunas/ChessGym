plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.puzzles.streak.vm"
    di = true
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":screens:common"))
    implementation(project(":settings:application:api"))
    implementation(project(":user:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.public.timber)

    testImplementation(testFixtures(project(":user:api")))
    testImplementation(testFixtures(project(":domain:api")))
    testImplementation(project(":game:logic:impl"))
    testImplementation(testFixtures(project(":settings:application:api")))
}
