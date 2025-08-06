package plugins.extensions.delegates

import org.gradle.api.Project
import plugins.extensions.android
import kotlin.reflect.KProperty

internal class AndroidNamespaceDelegate(private val project: Project) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? = project.android { namespace }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        value?.let {
            project.android {
                namespace = it
            }
        }
    }
}
