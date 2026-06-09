plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.navigation"
    di = true
}

dependencies {
    implementation(project(":global:qualifiers"))
}
