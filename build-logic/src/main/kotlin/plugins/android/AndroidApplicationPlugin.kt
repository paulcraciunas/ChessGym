package plugins.android

import com.android.build.api.dsl.ApplicationExtension
import common.androidTestImplementation
import common.bundle
import common.configureJava
import common.configureKotlin
import common.extension
import common.implementation
import common.addLintChecks
import common.configureAndroidLint
import common.library
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
            apply(libs.plugin("jetbrains-kotlin-serialization"))
            apply(libs.plugin("compose-compiler"))
            apply(libs.plugin("google-hilt"))
            apply(libs.plugin("google-ksp"))
        }
        configureComposeCompilerReports()
    }

    internal fun Project.configureAndroid(extension: ApplicationExtension) {
        configureJava(projectConfig.jvm)
        configureKotlin(projectConfig.jvm)
        val buildNumber = (findProperty("buildNumber") as? String)?.toIntOrNull() ?: 100
        val major = projectConfig.version.major
        val minor = projectConfig.version.minor

        extension.apply {
            compileSdk = projectConfig.android.compileSdk

            defaultConfig {
                minSdk = projectConfig.android.minSdk
                targetSdk = projectConfig.android.targetSdk
                versionCode = buildNumber
                versionName = "$major.$minor.$buildNumber"

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
                create("benchmark") {
                    initWith(getByName("release"))
                    matchingFallbacks += listOf("release")
                    isDebuggable = false
                }
                create("baselineProfile") {
                    initWith(getByName("benchmark"))
                    matchingFallbacks += listOf("benchmark", "release")
                    isMinifyEnabled = false
                    isShrinkResources = false
                }
            }
            buildFeatures {
                compose = true
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
            implementation(libs.library("kotlinx-serialization-json"))
            includeCompose(libs)
            includeDi(libs)
        }
    }

    private fun Project.configureTests() {
        dependencies {
            testImplementationBundle(libs.bundle("unit-tests"))
            testImplementation(libs.library("kotlinx-coroutines-test"))
            testRuntimeOnly(libs.library("junit-platform-launcher"))
            androidTestImplementation(libs.library("androidx-junit"))
            androidTestImplementation(libs.library("androidx-espresso-core"))
            val bom = platform(libs.library("androidx-compose-bom"))
            androidTestImplementation(bom)
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
