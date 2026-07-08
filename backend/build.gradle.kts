plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("io.ktor.plugin") version "3.4.3"
}

group = "com.paulcraciunas.chessgym"
version = "0.1.0"

application {
    mainClass.set("com.paulcraciunas.chessgym.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("chessgym-backend.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Force patched Jackson to resolve transitive CVEs from firebase-admin
    constraints {
        implementation("com.fasterxml.jackson.core:jackson-core:2.22.0")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.22.0")
        implementation("com.fasterxml.jackson.core:jackson-annotations:2.22")
    }

    // Ktor Server
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-rate-limit")
    implementation("io.ktor:ktor-server-swagger")

    // Ktor Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // Firebase Admin SDK (includes Firestore)
    implementation("com.google.firebase:firebase-admin:9.8.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.36")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
