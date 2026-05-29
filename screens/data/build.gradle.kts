plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.data"
    compose = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:logic:builders"))

    testImplementation(project(":game:logic:impl"))
}
