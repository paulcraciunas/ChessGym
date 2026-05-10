plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.notifications"
    di = true
}

dependencies {
    implementation(libs.androidx.appcompat)
}
