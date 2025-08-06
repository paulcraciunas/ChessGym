package plugins.extensions.delegates

import com.google.devtools.ksp.gradle.KspExtension
import common.implementation
import common.ksp
import common.library
import common.libs
import common.plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import kotlin.reflect.KProperty

internal class RoomDelegate(private val project: Project) {
    private var set: Boolean = false
    private var applied: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = applied

    operator fun setValue(thisRef: Any?, property: KProperty<*>, enabled: Boolean) {
        if (set) {
            throw IllegalStateException("The compose setting has already been configured once")
        }
        set = true
        applied = enabled
        if (enabled) {
            with(project) {
                pluginManager.apply(libs.plugin("google-ksp"))

                extensions.getByType<KspExtension>().apply {
                    arg("room.schemaLocation", "$projectDir/schemas")
                }

                dependencies {
                    implementation(libs.library("room-runtime"))
                    implementation(libs.library("room-ktx"))
                    ksp(libs.library("room-compiler"))
                }
            }
        }
    }
}
