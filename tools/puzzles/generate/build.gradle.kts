plugins {
    id("application")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":game:logic"))
    implementation(project(":game:io"))

    implementation(libs.kotlinx.coroutines.core)
}

application {
    mainClass = "com.paulcraciunas.tools.puzzles.generate.Generate"
    applicationDefaultJvmArgs = listOf("-Dgreeting.language=en")
}