package plugins.extensions

import org.gradle.api.Project
import plugins.extensions.delegates.AndroidTestFixturesDelegate
import plugins.extensions.delegates.ComposeDelegate
import plugins.extensions.delegates.DiDelegate
import plugins.extensions.delegates.RoomDelegate
import plugins.extensions.delegates.SerializationDelegate

open class AndroidConventionExtension(project: Project) : BaseAndroidExtension(project) {
    var di: Boolean by DiDelegate(project)
    var compose: Boolean by ComposeDelegate(project)
    var room: Boolean by RoomDelegate(project)
    var serialization: Boolean by SerializationDelegate(project)
    var testFixtures: Boolean by AndroidTestFixturesDelegate(project)
}
