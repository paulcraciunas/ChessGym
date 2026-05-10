plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.user.impl"
    serialization = true
    di = true
}

dependencies {
    api(project(":user:api"))
    implementation(project(":global:extensions"))

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.public.timber)

    testImplementation(testFixtures(project(":user:api")))
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
