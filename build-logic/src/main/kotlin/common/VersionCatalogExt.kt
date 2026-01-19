package common

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(name: String) = findLibrary(name).get()

internal fun VersionCatalog.bundle(name: String) = findBundle(name).get()

internal fun VersionCatalog.plugin(name: String) = findPlugin(name).get().get().pluginId

internal fun VersionCatalog.version(name: String) = findVersion(name).get().toString()
