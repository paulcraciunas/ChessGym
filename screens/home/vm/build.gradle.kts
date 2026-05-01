plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.home.vm"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":user:api"))
    api(project(":domain:api"))

    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":user:impl"))
}
