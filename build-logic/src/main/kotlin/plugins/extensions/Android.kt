package plugins.extensions

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal fun <T> Project.android(lambda: LibraryExtension.() -> T): T =
    with(extensions.getByType<LibraryExtension>()) { return@with lambda() }
