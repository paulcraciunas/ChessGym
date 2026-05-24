@file:Suppress("UnstableApiUsage")

package plugins.android

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import common.debugImplementation
import common.implementation
import common.ksp
import common.library
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun DependencyHandlerDelegate.includeCoreAndroid(libs: VersionCatalog) {
    implementation(libs.library("androidx-core-ktx"))
    implementation(libs.library("kotlinx-coroutines-android"))
}

internal fun DependencyHandlerDelegate.includeCompose(libs: VersionCatalog) {
    val bom = platform(libs.library("androidx-compose-bom"))
    implementation(bom)
    implementation(libs.library("androidx-activity-compose"))
    implementation(libs.library("material"))
    implementation(libs.library("androidx-material3"))
    implementation(libs.library("androidx-compose-material-icons-core"))
    implementation(libs.library("androidx-ui"))

    implementation(libs.library("androidx-ui-tooling-preview"))

    debugImplementation(libs.library("androidx-ui-tooling"))
    debugImplementation(libs.library("androidx-ui-test-manifest"))
}

internal fun DependencyHandlerDelegate.includeDi(libs: VersionCatalog) {
    implementation(libs.library("hilt-android"))
    ksp(libs.library("hilt-compiler"))
    ksp(libs.library("androidx-hilt-compiler"))
}

private const val CI_DEVICE_NAME = "ciDevice"
private const val CI_DEVICE_PROFILE = "Pixel 6"
private const val CI_DEVICE_API_LEVEL = 34
private const val CI_DEVICE_IMAGE_SOURCE = "aosp-atd"

internal fun ApplicationExtension.configureManagedDevices() {
    testOptions {
        managedDevices {
            localDevices {
                create(CI_DEVICE_NAME) { configureCiDevice() }
            }
        }
    }
}

internal fun LibraryExtension.configureManagedDevices() {
    testOptions {
        managedDevices {
            localDevices {
                create(CI_DEVICE_NAME) { configureCiDevice() }
            }
        }
    }
}

internal fun Project.configureComposeCompilerReports() {
    val reportsEnabled = findProperty("composeCompilerReports") == "true"
    if (reportsEnabled) {
        val outputDir = layout.buildDirectory.dir("compose_compiler").get().asFile.absolutePath
        tasks.withType(KotlinCompile::class.java) {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$outputDir",
                    "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$outputDir",
                )
            }
        }
    }
}

private fun ManagedVirtualDevice.configureCiDevice() {
    device = CI_DEVICE_PROFILE
    apiLevel = CI_DEVICE_API_LEVEL
    systemImageSource = CI_DEVICE_IMAGE_SOURCE
    require64Bit = true
}
