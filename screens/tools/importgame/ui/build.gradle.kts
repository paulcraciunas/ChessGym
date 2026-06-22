plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.importgame.ui"
    compose = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    api(project(":screens:tools:importgame:vm"))
}
