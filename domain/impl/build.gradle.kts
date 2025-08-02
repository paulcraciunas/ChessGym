plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":domain:api"))
    implementation(project(":game:logic:api"))
    implementation(project(":game:puzzles:api"))
    implementation(project(":settings:application:api"))
    implementation(project(":user:api"))
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)

    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.unit.tests)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":settings:application:api")))
    testImplementation(testFixtures(project(":game:puzzles:api")))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
