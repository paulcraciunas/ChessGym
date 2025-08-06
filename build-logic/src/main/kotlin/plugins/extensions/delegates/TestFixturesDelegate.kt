package plugins.extensions.delegates

import common.library
import common.libs
import common.testFixturesImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import kotlin.reflect.KProperty

internal class TestFixturesDelegate(private val project: Project) {
    private var set: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = with(project) {
        pluginManager.hasPlugin("java-test-fixtures")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, enabled: Boolean) {
        if (set) {
            throw IllegalStateException("The di setting has already been configured once")
        }
        set = true
        if (enabled) {
            with(project) {
                pluginManager.apply("java-test-fixtures")

                dependencies {
                    testFixturesImplementation(project.libs.library("kotlinx-coroutines-core"))
                }
            }
        }
    }
}
