package plugins.extensions.delegates

import common.implementation
import common.library
import common.libs
import common.plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import kotlin.reflect.KProperty

internal class SerializationDelegate(private val project: Project) {
    private var set: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = with(project) {
        pluginManager.hasPlugin(libs.plugin("jetbrains-kotlin-serialization"))
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, enabled: Boolean) {
        if (set) {
            throw IllegalStateException("The di setting has already been configured once")
        }
        set = true
        if (enabled) {
            with(project) {
                pluginManager.apply(libs.plugin("jetbrains-kotlin-serialization"))

                dependencies {
                    implementation(libs.library("kotlinx-serialization-json"))
                }
            }
        }
    }
}
