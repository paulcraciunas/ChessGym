package plugins.extensions

import org.gradle.api.Project
import plugins.extensions.delegates.ApplicationIdDelegate

open class AndroidApplicationExtension(project: Project) : BaseAndroidExtension(project) {
    var applicationId: String? by ApplicationIdDelegate(project)
}
