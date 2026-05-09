plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.settings.application.impl"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    api(project(":settings:application:api"))
    implementation(project(":global:extensions"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.public.timber)
}
