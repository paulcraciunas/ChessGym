package plugins.extensions

import org.gradle.api.Project
import plugins.extensions.delegates.SerializationDelegate
import plugins.extensions.delegates.TestFixturesDelegate

open class LibraryConventionExtension(val project: Project) {
    var serialization: Boolean by SerializationDelegate(project)
    var testFixtures: Boolean by TestFixturesDelegate(project)
}
