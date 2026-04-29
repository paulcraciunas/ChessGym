package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

object LintDebug : BuildType({
    id("LintDebug")
    name = "Lint Debug"
    description = "Runs all custom lint checks across all modules (debug variant)"

    // Capture HTML and XML reports for all modules
    artifactRules = "**/build/reports/lint-results*.html => lint-reports"

    applyCommonConfiguration()

    // Don't run lint if the basic build fails
    dependencies {
        snapshot(BuildDebug) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    steps {
        gradle {
            name = "Run Lint Checks"
            // Removed 'clean' to save time; --no-build-cache handles fresh execution
            tasks = "lintAllDebug"
            useGradleWrapper = true
            // --no-build-cache: Forces a full re-scan of the codebase
            // --continue: Find all lint errors across all modules, don't stop at the first one
            gradleParams = "--continue --no-build-cache"
        }
    }

    failureConditions {
        errorMessage = true
        nonZeroExitCode = true
    }
})
