package plugins.extensions.delegates

import org.gradle.api.Project
import plugins.extensions.commonAndroid
import kotlin.reflect.KProperty

internal class AndroidNamespaceDelegate(private val project: Project) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? = project.commonAndroid { namespace }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        value?.let {
            project.commonAndroid {
                namespace = it
            }
        }
    }
}
