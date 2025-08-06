package plugins.android

import common.debugImplementation
import common.implementation
import common.ksp
import common.library
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate

internal fun DependencyHandlerDelegate.includeCoreAndroid(libs: VersionCatalog) {
    implementation(libs.library("androidx-core-ktx"))
    implementation(libs.library("kotlinx-coroutines-android"))
}

internal fun DependencyHandlerDelegate.includeCompose(libs: VersionCatalog) {
    implementation(platform(libs.library("androidx-compose-bom")))
    implementation(libs.library("androidx-activity-compose"))
    implementation(libs.library("material"))
    implementation(libs.library("androidx-material3"))
    implementation(libs.library("androidx-ui"))

    debugImplementation(libs.library("androidx-ui-tooling-preview"))
    debugImplementation(libs.library("androidx-ui-tooling"))
    debugImplementation(libs.library("androidx-ui-test-manifest"))
}

internal fun DependencyHandlerDelegate.includeDi(libs: VersionCatalog) {
    implementation(libs.library("hilt-android"))
    ksp(libs.library("hilt-compiler"))
    ksp(libs.library("androidx-hilt-compiler"))
}
