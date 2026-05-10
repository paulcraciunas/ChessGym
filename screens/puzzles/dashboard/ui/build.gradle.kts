plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.puzzles.dashboard.ui"
    compose = true
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    api(project(":screens:puzzles:dashboard:vm"))
}
