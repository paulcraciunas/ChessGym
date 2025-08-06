package plugins.extensions.delegates

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import kotlin.reflect.KProperty

internal class ApplicationIdDelegate(private val project: Project) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? = with(project.extensions.getByType<ApplicationExtension>()) {
        defaultConfig.applicationId
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        value?.let { newValue ->
            project.extensions.getByType<ApplicationExtension>().defaultConfig.applicationId = newValue
        }
    }
}
