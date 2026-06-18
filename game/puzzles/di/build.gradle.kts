plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.puzzles.di"
    di = true
    benchmark = true
}

dependencies {
    api(project(":game:puzzles:api"))
    implementation(project(":game:puzzles:impl"))
    implementation(project(":game:serializer:api"))
    implementation(project(":global:notifications"))

    // Database
    implementation(libs.room.runtime)

    // Dependency injection
    implementation(libs.androidx.hilt.work)

    "benchmarkImplementation"(project(":game:logic:impl"))
}
