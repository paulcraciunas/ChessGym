package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

object InstrumentedTests : BuildType({
    id("InstrumentedTests")
    name = "Instrumented Tests"
    description = "Runs all Android instrumented/UI tests on a Gradle Managed Device"

    // Capture both HTML reports and raw XML results
    artifactRules = """
        **/build/reports/androidTests/** => instrumented-test-reports
        **/build/outputs/androidTest-results/** => instrumented-test-results
    """.trimIndent()

    params {
        param(
            "env.ANDROID_USER_HOME",
            """C:\android-user-home""",
        )
    }

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
            name = "Run Instrumented Tests"
            // Removed 'clean' to save time; --no-build-cache handles fresh execution
            tasks = "instrumentedTestAllCi"
            useGradleWrapper = true
            gradleParams = """
                --continue 
                --no-build-cache 
                -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
                -Dorg.gradle.workers.max=2
            """.trimIndent()
        }
    }

    failureConditions {
        errorMessage = true
        nonZeroExitCode = true
        testFailure = true
        // Prevent build from hanging forever (e.g., 30 minutes limit)
        executionTimeoutMin = 30
    }
})
