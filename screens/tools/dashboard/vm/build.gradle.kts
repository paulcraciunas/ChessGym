plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.screens.tools.dashboard.vm"
    di = true
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
