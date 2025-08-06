package plugins.extensions.delegates

import common.library
import common.libs
import common.testFixturesImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import plugins.extensions.android
import kotlin.reflect.KProperty

@Suppress("UnstableApiUsage")
internal class AndroidTestFixturesDelegate(private val project: Project) {
    private var set: Boolean = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = project.android { testFixtures.enable }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, enabled: Boolean) {
        if (set) {
            throw IllegalStateException("The di setting has already been configured once")
        }
        set = true
        if (enabled) {
            with(project) {
                android {
                    @Suppress("UnstableApiUsage")
                    testFixtures.enable = true
                }
                dependencies {
                    testFixturesImplementation(libs.library("kotlinx-coroutines-android"))
                }
            }
        }
    }
}
