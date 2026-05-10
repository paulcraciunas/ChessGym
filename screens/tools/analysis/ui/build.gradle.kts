plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.analysis.ui"
    compose = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:engine:api"))
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    api(project(":screens:tools:analysis:vm"))
}
