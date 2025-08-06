plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.resources"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
}
