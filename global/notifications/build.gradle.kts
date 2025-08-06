plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.notifications"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(libs.androidx.appcompat)
}
