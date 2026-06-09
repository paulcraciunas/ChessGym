plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.data"
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:engine:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))
    implementation(project(":settings:application:api"))

    implementation(libs.public.timber)

    testImplementation(project(":game:logic:impl"))
    testImplementation(testFixtures(project(":game:engine:api")))
    testImplementation(testFixtures(project(":settings:application:api")))
}
