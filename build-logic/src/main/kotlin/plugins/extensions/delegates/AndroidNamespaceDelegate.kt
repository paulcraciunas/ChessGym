package plugins.extensions.delegates

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import kotlin.reflect.KProperty

internal class AndroidNamespaceDelegate(private val project: Project) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        val lib = project.extensions.findByType<LibraryExtension>()
        if (lib != null) return lib.namespace
        return project.extensions.findByType<ApplicationExtension>()?.namespace
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        value?.let {
            val lib = project.extensions.findByType<LibraryExtension>()
            if (lib != null) {
                lib.namespace = it
                return
            }
            project.extensions.findByType<ApplicationExtension>()?.namespace = it
        }
    }
}
