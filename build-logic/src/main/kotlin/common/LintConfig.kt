package common

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import org.gradle.api.Project

internal fun ApplicationExtension.configureAndroidLint(project: Project) {
    lint {
        configureLint(project)
    }
}

internal fun LibraryExtension.configureAndroidLint(project: Project) {
    lint {
        configureLint(project)
    }
}

internal fun Project.configureLint() {
    extensions.configure<Lint>("lint") {
        configureLint(this@configureLint)
    }
}

private fun Lint.configureLint(project: Project) {
    lintConfig = project.rootProject.file("lint-rules/xml")
    warningsAsErrors = true
    abortOnError = true
    checkTestSources = true
    textReport = true
    baseline = null
}
