plugins {
    id("conventions.android.library")
}

androidLibrary {
    namespace = "com.paulcraciunas.puzzles.impl"
    di = true
    room = true
    instrumentedTests = true
}

dependencies {
    implementation(project(":game:puzzles:api"))
    implementation(project(":game:logic:di"))
    implementation(project(":game:serializer:api"))
    implementation(project(":global:qualifiers"))
    implementation(project(":global:utils"))
    implementation(project(":global:notifications"))
    implementation(project(":settings:application:api"))

    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.ktx)

    // Unpacking library
    implementation(libs.public.zstd) { artifact { type = "aar" } }
    implementation(libs.public.timber)

    testImplementation(testFixtures(project(":settings:application:api")))

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.work.test)
    androidTestImplementation(testFixtures(project(":settings:application:api")))
}
