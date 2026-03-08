plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)

    testImplementation(libs.lint.api)
    testImplementation(libs.lint.checks)
    testImplementation(libs.lint.tests)
    testImplementation(libs.junit)
}

tasks.withType<Jar> {
    manifest {
        attributes("Lint-Registry-v2" to "com.paulcraciunas.lint.ChessGymIssueRegistry")
    }
}
