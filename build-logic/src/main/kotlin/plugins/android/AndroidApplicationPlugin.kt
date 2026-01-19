package plugins.android

import com.android.build.api.dsl.ApplicationExtension
import common.androidTestImplementation
import common.bundle
import common.configureJava
import common.configureKotlin
import common.extension
import common.implementation
import common.library
import common.libs
import common.plugin
import common.testImplementation
import common.testImplementationBundle
import common.version
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import plugins.ConventionPlugin
import plugins.extensions.AndroidApplicationExtension

@Suppress("unused") // Declared in build.gradle.kts
class AndroidApplicationPlugin : ConventionPlugin() {
    private lateinit var configuration: AndroidApplicationExtension

    override fun apply(target: Project) {
        configuration = target.extension("chessGymApp")
        with(target) {
            configurePlugins()
            configureAndroid(extension = extensions.getByType<ApplicationExtension>())
            configureDependencies()
            configureTests()
        }
    }

    private fun Project.configurePlugins() {
        with(pluginManager) {
            apply(libs.plugin("android-application"))
            apply(libs.plugin("jetbrains-kotlin-android"))
            apply(libs.plugin("jetbrains-kotlin-serialization"))
            apply(libs.plugin("compose-compiler"))
            apply(libs.plugin("google-hilt"))
            apply(libs.plugin("google-ksp"))
        }
    }

    internal fun Project.configureAndroid(extension: ApplicationExtension) {
        configureJava(projectConfig.jvm)
        configureKotlin(projectConfig.jvm)
        extension.apply {
            compileSdk = projectConfig.android.compileSdk

            defaultConfig {
                minSdk = projectConfig.android.minSdk
                targetSdk = projectConfig.android.targetSdk
                versionCode = projectConfig.version.code
                versionName = projectConfig.version.name

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                vectorDrawables {
                    useSupportLibrary = true
                }
            }

            buildTypes {
                debug {
                    isMinifyEnabled = false
                }
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
                }
            }
            buildFeatures {
                compose = true
            }
            composeOptions {
                kotlinCompilerExtensionVersion = libs.version("compose-compiler")
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }
    }

    private fun Project.configureDependencies() {
        dependencies {
            includeCoreAndroid(libs)
            implementation(libs.library("kotlinx-serialization-json"))
            includeCompose(libs)
            includeDi(libs)
        }
    }

    private fun Project.configureTests() {
        dependencies {
            testImplementationBundle(libs.bundle("unit-tests"))
            testImplementation(libs.library("kotlinx-coroutines-test"))
            androidTestImplementation(libs.library("androidx-junit"))
            androidTestImplementation(libs.library("androidx-espresso-core"))
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
