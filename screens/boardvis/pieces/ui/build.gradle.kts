plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.boardvis.pieces.ui"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    compose = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":global:resources"))
    implementation(project(":screens:common"))
    implementation(project(":settings:application:api"))
    api(project(":screens:boardvis:pieces:vm"))
}
