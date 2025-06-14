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

    testImplementation(libs.bundles.unit.tests)
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
