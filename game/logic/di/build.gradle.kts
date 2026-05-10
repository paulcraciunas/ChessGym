plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.logic.di"
    di = true
}

dependencies {
    api(project(":game:logic:api"))
    implementation(project(":game:logic:impl"))
}
