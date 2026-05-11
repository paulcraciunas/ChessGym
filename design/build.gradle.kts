plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.design"
    compose = true
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":game:logic:api"))
}