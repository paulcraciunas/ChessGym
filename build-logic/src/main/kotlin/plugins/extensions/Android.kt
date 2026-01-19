package plugins.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

internal fun <T> Project.android(lambda: LibraryExtension.() -> T): T =
    with(extensions.getByType<LibraryExtension>()) { return@with lambda() }

internal fun <T> Project.app(lambda: ApplicationExtension.() -> T): T =
    with(extensions.getByType<ApplicationExtension>()) { return@with lambda() }

internal inline fun <T> Project.commonAndroid(lambda: CommonExtension<*, *, *, *, *, *>.() -> T): T =
    with(extensions.findByType<LibraryExtension>() ?: extensions.findByType<ApplicationExtension>()!!) {
        return@with lambda()
    }
