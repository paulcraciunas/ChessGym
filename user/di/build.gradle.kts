plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.user.di"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    api(project(":user:api"))
    implementation(project(":global:qualifiers"))
    implementation(project(":user:impl"))
    implementation(project(":user:remote"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.public.timber)
    implementation(libs.firebase.auth)

    implementation(libs.androidx.work.ktx)
}
