package plugins

import common.ProjectConfig
import common.ProjectConfig.Android
import common.ProjectConfig.Jvm
import common.ProjectConfig.Version
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class ConventionPlugin : Plugin<Project> {
    val projectConfig: ProjectConfig = object : ProjectConfig {
        override val jvm: Jvm = object : Jvm {
            override val version: Int = 17
        }
        override val android: Android = object : Android {
            override val compileSdk: Int = 36
            override val targetSdk: Int = 36
            override val minSdk: Int = 27
        }
        override val version: Version = object : Version {
            override val code: Int = 1
            override val name: String = "1.0"
        }
    }
}
