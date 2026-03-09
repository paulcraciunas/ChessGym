package plugins.library

import common.addLintChecks
import common.bundle
import common.configureJava
import common.configureKotlin
import common.configureLint
import common.extension
import common.implementation
import common.library
import common.libs
import common.plugin
import common.testImplementation
import common.testImplementationBundle
import common.testRuntimeOnly
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import plugins.ConventionPlugin
import plugins.extensions.LibraryConventionExtension

@Suppress("unused") // Declared in build.gradle.kts
class LibraryConventionPlugin : ConventionPlugin() {
    private lateinit var configuration: LibraryConventionExtension

    override fun apply(target: Project) {
        configuration = target.extension("library")
        with(target) {
            configurePlugins()
            configureJava(projectConfig.jvm)
            configureKotlin(projectConfig.jvm)
            configureDependencies()
            configureTests()
        }
    }

    private fun Project.configurePlugins() {
        with(pluginManager) {
            apply("java-library")
            apply(libs.plugin("jetbrains-kotlin-jvm"))
            apply("com.android.lint")
        }
        configureLint()
    }

    private fun Project.configureDependencies() {
        addLintChecks(":lint-rules")
        dependencies {
            implementation(libs.library("kotlinx-coroutines-core"))
        }
    }

    private fun Project.configureTests() {
        dependencies {
            testRuntimeOnly(libs.library("junit-platform-launcher"))
            testImplementationBundle(libs.bundle("unit-tests"))
            testImplementation(libs.library("kotlinx-coroutines-test"))
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
