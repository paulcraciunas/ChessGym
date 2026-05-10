package plugins.android

import com.android.build.api.dsl.LibraryExtension
import common.androidTestImplementation
import common.bundle
import common.configureJava
import common.configureKotlin
import common.extension
import common.library
import common.addLintChecks
import common.configureAndroidLint
import common.libs
import common.plugin
import common.testImplementation
import common.testImplementationBundle
import common.testRuntimeOnly
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import plugins.ConventionPlugin
import plugins.extensions.AndroidConventionExtension

@Suppress("unused") // Declared in build.gradle.kts
class AndroidConventionPlugin : ConventionPlugin() {
    private lateinit var configuration: AndroidConventionExtension

    override fun apply(target: Project) {
        configuration = target.extension("androidLibrary")
        with(target) {
            configurePlugins()
            configureAndroid(extension = extensions.getByType<LibraryExtension>())
            configureDependencies()
            configureTests()
        }
    }

    private fun Project.configurePlugins() {
        with(pluginManager) {
            apply(libs.plugin("android-library"))
        }
    }

    internal fun Project.configureAndroid(extension: LibraryExtension) {
        configureJava(projectConfig.jvm)
        configureKotlin(projectConfig.jvm)
        extension.apply {
            compileSdk = projectConfig.android.compileSdk

            defaultConfig {
                minSdk = projectConfig.android.minSdk

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                debug {
                    isMinifyEnabled = false
                }
                release {
                    isMinifyEnabled = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
                }
            }
            configureManagedDevices()
            configureAndroidLint(this@configureAndroid)
        }
    }

    private fun Project.configureDependencies() {
        addLintChecks(":lint-rules")
        dependencies {
            includeCoreAndroid(libs)
        }
    }

    private fun Project.configureTests() {
        dependencies {
            testImplementationBundle(libs.bundle("unit-tests"))
            testImplementation(libs.library("kotlinx-coroutines-test"))
            testRuntimeOnly(libs.library("junit-platform-launcher"))
            androidTestImplementation(libs.library("androidx-junit"))
            androidTestImplementation(libs.library("androidx-espresso-core"))
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
