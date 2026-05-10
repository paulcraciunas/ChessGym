plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.global.extensions"
}

dependencies {
    implementation(libs.public.timber)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
}
