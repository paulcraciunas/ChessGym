plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.blindmode.ui"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    compose = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    api(project(":screens:blindmode:vm"))
}
