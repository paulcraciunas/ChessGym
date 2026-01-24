plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.boardvis.squares.vm"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":user:api"))
    implementation(project(":screens:common"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(testFixtures(project(":user:api")))
}
