package plugins.extensions.delegates

import org.gradle.api.Project
import kotlin.reflect.KProperty

internal class InstrumentedTestDelegate(private val project: Project) {
    private var set: Boolean = false
    private var enabled: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = enabled

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        if (set) {
            throw IllegalStateException("The instrumentedTests setting has already been configured once")
        }
        set = true
        enabled = value
        if (value) {
            project.rootProject.tasks.named("instrumentedTestAllCi") {
                // DO NOT DO this:
                // dependsOn(project.tasks.named("ciDeviceDebugAndroidTest"))
                // as project.tasks resolves to Task.project (the root project), not the current subproject
                dependsOn("${project.path}:ciDeviceDebugAndroidTest")
            }
        }
    }
}
