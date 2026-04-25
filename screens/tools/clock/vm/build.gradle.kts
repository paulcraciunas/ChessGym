plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.clock.vm"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":game:logic:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
}
