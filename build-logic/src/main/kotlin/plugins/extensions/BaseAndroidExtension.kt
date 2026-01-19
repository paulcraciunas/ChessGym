package plugins.extensions

import org.gradle.api.Project
import plugins.extensions.delegates.AndroidNamespaceDelegate

abstract class BaseAndroidExtension(val project: Project) {
    var namespace: String? by AndroidNamespaceDelegate(project)
}
