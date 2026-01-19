package plugins.extensions.delegates

import org.gradle.api.Project
import plugins.extensions.app
import kotlin.reflect.KProperty

internal class ApplicationIdDelegate(private val project: Project) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? = project.app {
        defaultConfig.applicationId
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        value?.let { newValue ->
            project.app {
                defaultConfig.applicationId = newValue
            }
        }
    }
}
