plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.clock.ui"
    compose = true
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    implementation(project(":game:logic:api"))
    implementation(project(":domain:api"))
    api(project(":screens:tools:clock:vm"))
    implementation(libs.public.timber)
}
