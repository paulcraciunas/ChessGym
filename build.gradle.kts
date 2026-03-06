// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.kapt) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.hilt) apply false
    alias(libs.plugins.google.ksp) apply false
}

tasks.register("unitTestAllDebug") {
    group = "verification"
    description = "Runs debug unit tests for Android modules and all tests for JVM modules"
}

tasks.register("unitTestAllRelease") {
    group = "verification"
    description = "Runs release unit tests for Android modules and all tests for JVM modules"
}

subprojects {
    plugins.withId("com.android.library") {
        rootProject.tasks.named("unitTestAllDebug") {
            dependsOn(tasks.named("testDebugUnitTest"))
        }
        rootProject.tasks.named("unitTestAllRelease") {
            dependsOn(tasks.named("testReleaseUnitTest"))
        }
    }
    plugins.withId("com.android.application") {
        rootProject.tasks.named("unitTestAllDebug") {
            dependsOn(tasks.named("testDebugUnitTest"))
        }
        rootProject.tasks.named("unitTestAllRelease") {
            dependsOn(tasks.named("testReleaseUnitTest"))
        }
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        rootProject.tasks.named("unitTestAllDebug") {
            dependsOn(tasks.named("test"))
        }
        rootProject.tasks.named("unitTestAllRelease") {
            dependsOn(tasks.named("test"))
        }
    }
}