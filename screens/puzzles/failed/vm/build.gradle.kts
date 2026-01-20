plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.puzzles.failed.vm"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":screens:common"))
    implementation(project(":settings:application:api"))
    implementation(project(":user:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":settings:application:api")))
    testImplementation(testFixtures(project(":user:api")))
    testImplementation(project(":game:logic:impl"))
}
