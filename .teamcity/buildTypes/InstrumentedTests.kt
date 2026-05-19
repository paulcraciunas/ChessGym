package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildStep
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import kotlin.text.trimIndent

object InstrumentedTests : BuildType({
    id("InstrumentedTests")
    name = "Instrumented Tests"
    description = "Runs all Android instrumented/UI tests on a Gradle Managed Device"

    artifactRules = """
        instrumented-test-reports.zip
        instrumented-test-results.zip
    """.trimIndent()

    applyCommonConfiguration()

    // Don't run tests if the basic build fails
    dependencies {
        snapshot(BuildDebug) {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }
    }

    steps {
        // Startup clean - heal problems from previous builds
        killEmulatorsAndDaemons("Kill Stuck Emulators - Startup")
        clearLockFiles("Clear Leftover Lock Files - Startup")

        gradle {
            name = "Run Instrumented Tests"
            tasks = "instrumentedTestAllCi"
            useGradleWrapper = true
            gradleParams = """
                --continue 
                --build-cache 
                --parallel
                -Pandroid.experimental.androidTest.numManagedDeviceShards=2
                -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
                -Dorg.gradle.workers.max=4
                -Dkotlin.incremental=true
            """.trimIndent()
        }
        script {
            name = "Archive Test Reports"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            scriptContent = "powershell -NoProfile -ExecutionPolicy Bypass -File ci/archive-instrumented-reports.ps1"
        }

        // Teardown preparation
        killEmulatorsAndDaemons("Kill Stuck Emulators - Pre-Clean", BuildStep.ExecutionMode.ALWAYS)
        clearLockFiles("Clear Leftover Lock Files - Pre-Clean", BuildStep.ExecutionMode.ALWAYS)

        gradle {
            name = "Clean Managed Devices"
            executionMode = BuildStep.ExecutionMode.ALWAYS
            tasks = "cleanManagedDevices"
            useGradleWrapper = true
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
