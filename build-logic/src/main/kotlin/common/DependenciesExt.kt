package common

import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate

internal fun DependencyHandlerDelegate.implementation(dependency: Provider<MinimalExternalModuleDependency>) {
    add("implementation", dependency)
}

internal fun DependencyHandlerDelegate.debugImplementation(dependency: Provider<MinimalExternalModuleDependency>) {
    add("debugImplementation", dependency)
}

internal fun DependencyHandlerDelegate.ksp(dependency: Provider<MinimalExternalModuleDependency>) {
    add("ksp", dependency)
}

internal fun DependencyHandlerDelegate.testFixturesImplementation(dependency: Provider<MinimalExternalModuleDependency>) {
    add("testFixturesImplementation", dependency)
}

internal fun DependencyHandlerDelegate.testRuntimeOnly(dependency: Provider<MinimalExternalModuleDependency>) {
    add("testRuntimeOnly", dependency)
}

internal fun DependencyHandlerDelegate.testImplementation(dependency: Provider<MinimalExternalModuleDependency>) {
    add("testImplementation", dependency)
}

internal fun DependencyHandlerDelegate.testImplementationBundle(dependency: Provider<ExternalModuleDependencyBundle>) {
    add("testImplementation", dependency)
}

internal fun DependencyHandlerDelegate.androidTestImplementation(dependency: Provider<MinimalExternalModuleDependency>) {
    add("androidTestImplementation", dependency)
}

internal fun DependencyHandlerDelegate.api(dependency: Provider<MinimalExternalModuleDependency>) {
    add("api", dependency)
}

internal fun org.gradle.api.Project.addLintChecks(projectPath: String) {
    dependencies.add("lintChecks", dependencies.project(mapOf("path" to projectPath)))
}
