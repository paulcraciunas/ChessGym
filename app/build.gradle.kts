plugins {
    id("conventions.android.app")
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
    defaultConfig {
        val majorVersion = "1"
        val minorVersion = "0"
        buildConfigField("String", "APP_VERSION", "\"$majorVersion.$minorVersion\"")
        testInstrumentationRunner = "com.paulcraciunas.chessgym.runner.HiltTestRunner"
    }

    testBuildType = "uitest"
    buildTypes {
        debug {
            applicationIdSuffix = ".debug" // This makes it "com.paulcraciunas.chessgym.debug"
            versionNameSuffix = "-debug"

            val debugBuildNumber = "100"
            buildConfigField("String", "BUILD_NUMBER", "\"$debugBuildNumber\"")
        }
        create("uitest") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".uitest"
            versionNameSuffix = "-uitest"
            matchingFallbacks += listOf("debug")
        }
        release {
            val releaseBuildNumber: String = project.findProperty("buildNumber") as? String ?: "0"
            buildConfigField("String", "BUILD_NUMBER", "\"$releaseBuildNumber\"")
        }
    }

    sourceSets {
        getByName("androidTest") {
            // This ensures both debug and uitest use the same test folder
            java.srcDirs("src/androidTest/java")
            kotlin.srcDirs("src/androidTest/java")
        }
    }
}

dependencies {
    implementation(project(":game:puzzles:di"))
    implementation(project(":game:logic:di"))
    implementation(project(":game:engine:impl"))
    implementation(project(":game:serializer:di"))
    implementation(project(":domain:di"))
    implementation(project(":domain:api"))
    implementation(project(":global:device:di"))
    implementation(project(":global:notifications"))
    implementation(project(":global:resources"))
    implementation(project(":global:utils"))
    implementation(project(":settings:application:impl"))
    implementation(project(":screens:common"))
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

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Edge-to-edge
    implementation(libs.androidx.activity.ktx)

    // Work Manager Integration
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)

    // Logging
    implementation(libs.public.timber)

    // UI Testing
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(project(":domain:api"))
    androidTestImplementation(project(":game:logic:api"))
    androidTestImplementation(project(":user:api"))
    androidTestImplementation(testFixtures(project(":user:api")))
    androidTestImplementation(testFixtures(project(":settings:application:api")))
    androidTestImplementation(testFixtures(project(":domain:api")))
}
