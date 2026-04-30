plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.achievements.vm"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    api(project(":domain:api"))
    implementation(project(":user:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
}
