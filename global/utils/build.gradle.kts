plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.utils"
    di = true
}

dependencies {
    api(project(":global:qualifiers"))
}
