plugins {
    id("java-library")
    id("java-test-fixtures")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":game:logic:api"))
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testFixturesImplementation(project(":game:logic:api"))
    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.junit.jupiter.api)
    testFixturesImplementation(libs.junit.jupiter.engine)
    testFixturesImplementation(libs.junit.jupiter.params)
    testFixturesImplementation(libs.junit.jupiter)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
