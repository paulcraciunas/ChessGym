plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.serializer.di"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":game:logic:api"))
    implementation(project(":game:serializer:impl"))
}
