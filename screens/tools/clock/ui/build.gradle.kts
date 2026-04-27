plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.clock.ui"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    compose = true
}

dependencies {
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    implementation(project(":game:logic:api"))
    implementation(project(":domain:api"))
    api(project(":screens:tools:clock:vm"))
}
