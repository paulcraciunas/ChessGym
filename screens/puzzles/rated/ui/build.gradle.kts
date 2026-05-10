plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.puzzles.rated.ui"
    compose = true
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    implementation(project(":game:logic:api"))
    api(project(":screens:puzzles:rated:vm"))
}
