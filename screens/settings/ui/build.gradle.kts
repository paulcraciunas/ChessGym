plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.settings.ui"
    compose = true
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":settings:application:api"))
    implementation(project(":screens:common"))
    api(project(":screens:settings:vm"))
}
