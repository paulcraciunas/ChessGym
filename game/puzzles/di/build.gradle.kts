plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.puzzles.di"
    consumerProguardFile("consumer-rules.pro")
    proguardFile("proguard-rules.pro")
    di = true
}

dependencies {
    api(project(":game:puzzles:api"))
    implementation(project(":game:puzzles:impl"))
    implementation(project(":game:logic:di"))
    implementation(project(":game:serializer:api"))
    implementation(project(":global:notifications"))

    // Database
    implementation(libs.room.runtime)

    // Dependency injection
    implementation(libs.androidx.hilt.work)
}
