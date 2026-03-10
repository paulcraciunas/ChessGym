package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

object UnitTests : BuildType({
    id("UnitTests")
    name = "Unit Tests"
    description = "Runs all unit tests across all modules (debug variant)"

    // Capture HTML reports for easy debugging in TeamCity
    artifactRules = "**/build/reports/tests/** => test-reports"

    applyCommonConfiguration()

    steps {
        gradle {
            name = "Run Unit Tests"
            // 'clean' ensures we start from a fresh state
            tasks = "clean unitTestAllDebug"
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
