package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

object PuzzleVerification : BuildType({
    id("PuzzleVerification")
    name = "Puzzle Verification"
    description = "Downloads the Lichess puzzle database and verifies serialization/deserialization and playthrough of all puzzles"

    applyCommonConfiguration()

    // Don't run verification if the basic build fails
    dependencies {
        snapshot(BuildDebug) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    // Clear triggers from common configuration to make this manual-only
    triggers.items.clear()

    steps {
        gradle {
            name = "Verify All Puzzles"
            tasks = ":tools:puzzle-verifier:run"
            useGradleWrapper = true
            gradleParams = "--no-build-cache"
        }
    }

    failureConditions {
        errorMessage = true
        nonZeroExitCode = true
    }
})
