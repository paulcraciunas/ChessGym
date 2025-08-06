package plugins.extensions

import org.gradle.api.Project
import plugins.extensions.delegates.AndroidNamespaceDelegate

abstract class BaseAndroidExtension(val project: Project) {
    var namespace: String? by AndroidNamespaceDelegate(project)

    fun consumerProguardFile(file: String) = project.android {
        defaultConfig {
            consumerProguardFiles(file)
        }
    }

    fun proguardFile(file: String) = project.android {
        buildTypes {
            release {
                proguardFiles(file)
            }
        }
    }
}
