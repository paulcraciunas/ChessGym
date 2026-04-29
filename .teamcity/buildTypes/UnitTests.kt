package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

object UnitTests : BuildType({
    id("UnitTests")
    name = "Unit Tests"
    description = "Runs all unit tests across all modules (debug variant)"

    // Capture HTML reports for easy debugging in TeamCity
    artifactRules = "**/build/reports/tests/** => test-reports"

    applyCommonConfiguration()

    // Don't run tests if the basic build fails
    dependencies {
        snapshot(BuildDebug) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    steps {
        gradle {
            name = "Run Unit Tests"
            // Removed 'clean' to save time; --no-build-cache handles fresh execution
            tasks = "unitTestAllDebug"
            useGradleWrapper = true
            // --no-build-cache: Forces tests to run even if they were successful in a previous build
            // --continue: Runs all tests even if one module fails
            gradleParams = "--continue --no-build-cache"
        }
    }

    failureConditions {
        errorMessage = true
        nonZeroExitCode = true
        testFailure = true
    }
})
