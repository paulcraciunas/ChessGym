plugins {
    id("java-library")
    id("java-test-fixtures")
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
    implementation(project(":game:logic:api"))
    implementation(libs.kotlinx.coroutines.core)

    testFixturesImplementation(project(":game:logic:impl"))
    testFixturesImplementation(project(":game:serializer:impl"))
}
