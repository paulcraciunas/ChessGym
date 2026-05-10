plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.blindmode.ui"
    compose = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    api(project(":screens:blindmode:vm"))
}
