package plugins.extensions.delegates

import common.libs
import common.plugin
import common.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import plugins.android.includeCompose
import plugins.extensions.android
import kotlin.reflect.KProperty

internal class ComposeDelegate(private val project: Project) {
    private var set: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = with(project) {
        pluginManager.hasPlugin(libs.plugin("compose-compiler"))
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, enabled: Boolean) {
        if (set) {
            throw IllegalStateException("The compose setting has already been configured once")
        }
        set = true
        if (enabled) {
            with(project) {
                pluginManager.apply(libs.plugin("compose-compiler"))

                android {
                    buildFeatures {
                        compose = true
                    }
                    composeOptions {
                        kotlinCompilerExtensionVersion = libs.version("compose-compiler")
                    }
                }

                dependencies {
                    includeCompose(libs)
                }
            }
        }
    }
}
