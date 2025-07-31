plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.hilt)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.paulcraciunas.puzzles.impl"
    compileSdk = 35

    defaultConfig {
        minSdk = 27

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":game:puzzles:api"))
    implementation(project(":game:logic:di"))
    implementation(project(":game:serializer:api"))
    implementation(project(":global:notifications"))
    implementation(project(":settings:application:api"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.hilt.common)
    ksp(libs.room.compiler)

    // Dependency injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Unpacking library
    implementation(libs.public.zstd) { artifact { type = "aar" } }

    testFixturesImplementation(project(":game:puzzles:api"))
    testFixturesImplementation(libs.bundles.unit.tests)

    testImplementation(libs.bundles.unit.tests)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":settings:application:api")))
    testRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.work.test)
    androidTestImplementation(testFixtures(project(":settings:application:api")))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
