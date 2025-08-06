plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.user.impl"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    serialization = true
    di = true
}

dependencies {
    api(project(":user:api"))

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
}
