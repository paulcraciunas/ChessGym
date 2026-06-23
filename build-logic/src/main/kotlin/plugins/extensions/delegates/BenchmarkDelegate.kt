package plugins.extensions.delegates

import org.gradle.api.Project
import plugins.extensions.android
import kotlin.reflect.KProperty

internal class BenchmarkDelegate(private val project: Project) {
    private var set: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = set

    operator fun setValue(thisRef: Any?, property: KProperty<*>, enabled: Boolean) {
        if (set) {
            throw IllegalStateException("The benchmark setting has already been configured once")
        }
        set = true
        if (enabled) {
            with(project) {
                android {
                    buildTypes {
                        create("benchmark") {
                            initWith(getByName("release"))
                            matchingFallbacks += listOf("release")
                            isMinifyEnabled = false
                        }
                        create("baselineProfile") {
                            initWith(getByName("benchmark"))
                            matchingFallbacks += listOf("benchmark", "release")
                        }
                    }
                }
            }
        }
    }
}
