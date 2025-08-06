plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.domain.di"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":domain:impl"))
}
