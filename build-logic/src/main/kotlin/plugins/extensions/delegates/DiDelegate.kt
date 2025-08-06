package plugins.extensions.delegates

import common.libs
import common.plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import plugins.android.includeDi
import kotlin.reflect.KProperty

internal class DiDelegate(private val project: Project) {
    private var set: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = with(project) {
        pluginManager.hasPlugin(libs.plugin("google-hilt"))
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, enabled: Boolean) {
        if (set) {
            throw IllegalStateException("The di setting has already been configured once")
        }
        set = true
        if (enabled) {
            with(project) {
                pluginManager.apply(libs.plugin("google-hilt"))
                pluginManager.apply(libs.plugin("google-ksp"))

                dependencies {
                    includeDi(project.libs)
                }
            }
        }
    }
}
