package plugins.extensions.delegates

import common.androidTestImplementation
import common.library
import common.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
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
            with(project) {
                val subprojectTasks = project.tasks // Don't move this inside the lambda, or it'll break
                project.rootProject.tasks.named("instrumentedTestAllCi") {
                    dependsOn(subprojectTasks.matching { it.name == "ciDeviceDebugAndroidTest" })
                }

                dependencies {
                    androidTestImplementation(libs.library("androidx-junit"))
                    androidTestImplementation(libs.library("androidx-espresso-core"))
                    val bom = platform(libs.library("androidx-compose-bom"))
                    androidTestImplementation(bom)
                }
            }
        }
    }
}
