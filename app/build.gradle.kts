import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    id("conventions.android.app")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

chessGymApp {
    namespace = "com.paulcraciunas.chessgym"
    applicationId = "com.paulcraciunas.chessgym"
    proguardFile("proguard-rules.pro")
}

android {
    buildFeatures {
        buildConfig = true
    }
    bundle {
        // Ship all translations in the base APK rather than as on-demand language splits.
        // When a user changes the device language, Play would otherwise install the matching
        // language split into the already-running app, relocating the package's APK directory
        // and invalidating the live process's path to the native library (libstockfishjni.so),
        // causing an UnsatisfiedLinkError until the next cold start. Disabling language splits
        // removes that trigger; the size cost is negligible for string-only resources.
        language {
            enableSplit = false
        }
    }
    defaultConfig {
        testInstrumentationRunner = "com.paulcraciunas.chessgym.runner.HiltTestRunner"
    }

    val keystorePath = System.getenv("KEYSTORE_PATH")
    if (keystorePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    testBuildType = "uitest"
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "BACKEND_URL", "\"http://10.0.2.2:8080\"")
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        create("uitest") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".uitest"
            matchingFallbacks += listOf("debug")
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
        release {
            signingConfig = signingConfigs.getByName(if (keystorePath != null) "release" else "debug")

            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BACKEND_URL", "\"https://api.chessgym.uk\"")
        }
        getByName("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".benchmark"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-benchmark.pro",
            )
            buildConfigField("boolean", "ENABLE_TEST_TAGS", "true")
            buildConfigField("String", "BACKEND_URL", "\"https://api.chessgym.uk\"")
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            @Suppress("UnstableApiUsage")
            experimentalProperties["android.experimental.enableTestTagsAsResourceId"] = true
        }
        getByName("baselineProfile") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".benchmark"
            buildConfigField("boolean", "ENABLE_TEST_TAGS", "true")
            buildConfigField("String", "BACKEND_URL", "\"https://api.chessgym.uk\"")
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            @Suppress("UnstableApiUsage")
            experimentalProperties["android.experimental.enableTestTagsAsResourceId"] = true
        }
    }

    sourceSets {
        named("uitest") {
            java.directories.add("src/debug/java")
            kotlin.directories.add("src/debug/java")
        }
        named("androidTest") {
            java.directories.add("src/androidTest/java")
            kotlin.directories.add("src/androidTest/java")
        }
        named("benchmark") {
            java.directories.add("src/release/java")
            kotlin.directories.add("src/release/java")
        }
        named("baselineProfile") {
            java.directories.add("src/release/java")
            kotlin.directories.add("src/release/java")
            assets.directories.add("src/benchmark/assets")
        }
    }
}

dependencies {
    implementation(project(":game:puzzles:di"))
    implementation(project(":game:logic:builders"))
    implementation(project(":game:engine:impl"))
    implementation(project(":game:serializer:di"))
    implementation(project(":domain:di"))
    implementation(project(":domain:api"))
    implementation(project(":global:device:di"))
    implementation(project(":global:billing"))
    implementation(project(":global:navigation"))
    implementation(project(":global:notifications"))
    implementation(project(":global:qualifiers"))
    implementation(project(":global:resources"))
    implementation(project(":global:sounds"))
    implementation(project(":global:utils"))
    implementation(project(":settings:application:impl"))
    implementation(project(":screens:common"))
    implementation(project(":screens:data"))
    implementation(project(":screens:loading:ui"))
    implementation(project(":screens:home:ui"))
    implementation(project(":screens:puzzles:dashboard:ui"))
    implementation(project(":screens:puzzles:rated:ui"))
    implementation(project(":screens:puzzles:rush:ui"))
    implementation(project(":screens:puzzles:failed:ui"))
    implementation(project(":screens:puzzles:streak:ui"))
    implementation(project(":screens:boardvis:dashboard:ui"))
    implementation(project(":screens:boardvis:squares:ui"))
    implementation(project(":screens:boardvis:pieces:ui"))
    implementation(project(":screens:settings:ui"))
    implementation(project(":screens:signin:ui"))
    implementation(project(":screens:about:ui"))
    implementation(project(":screens:achievements:ui"))
    implementation(project(":screens:blindmode:ui"))
    implementation(project(":screens:tools:dashboard:ui"))
    implementation(project(":screens:tools:clock:ui"))
    implementation(project(":screens:tools:analysis:ui"))
    implementation(project(":screens:tools:importgame:ui"))
    implementation(project(":user:di"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui.graphics)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.androidx.hilt.viewmodel)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)

    // Edge-to-edge
    implementation(libs.androidx.activity.ktx)

    // Work Manager Integration
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)

    // Logging
    implementation(libs.public.timber)

    // Profile Installer
    implementation(libs.androidx.profileinstaller)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)

    // Credential Manager (Google Sign-In)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)

    // Google Play Store
    implementation(libs.google.play)
    implementation(libs.google.billing)

    // Unit Testing
    testImplementation(project(":domain:api"))
    testImplementation(project(":domain:impl"))
    testImplementation(project(":user:api"))
    testImplementation(testFixtures(project(":domain:api")))
    testImplementation(testFixtures(project(":user:api")))
    testImplementation(testFixtures(project(":settings:application:api")))
    testImplementation(testFixtures(project(":global:device:api")))

    // UI Testing
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(project(":domain:api"))
    androidTestImplementation(project(":domain:impl"))
    androidTestImplementation(project(":game:logic:api"))
    androidTestImplementation(project(":user:api"))
    androidTestImplementation(testFixtures(project(":user:api")))
    androidTestImplementation(testFixtures(project(":settings:application:api")))
    androidTestImplementation(testFixtures(project(":domain:api")))

    "benchmarkImplementation"(libs.androidx.tracing)
    "benchmarkImplementation"(libs.androidx.tracing.binary)
}

