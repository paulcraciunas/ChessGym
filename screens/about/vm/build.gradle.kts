plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.about.vm"
    di = true
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
