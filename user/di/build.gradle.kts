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
    implementation(project(":user:impl"))
    implementation(project(":user:remote"))
}
