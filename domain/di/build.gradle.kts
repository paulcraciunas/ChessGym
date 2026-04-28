plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.domain.di"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":domain:impl"))
    implementation(project(":game:engine:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":game:serializer:api"))
    implementation(project(":game:serializer:di"))
}
