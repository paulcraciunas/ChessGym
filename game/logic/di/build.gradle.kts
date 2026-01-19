plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.logic.di"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    api(project(":game:logic:api"))
    implementation(project(":game:logic:impl"))
}
