package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.ParameterDisplay
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object BuildRelease : BuildType({
    id("BuildRelease")
    name = "Build Release"
    description = "Builds a signed release AAB for Play Store deployment"

    artifactRules = "app/build/outputs/bundle/release/*.aab => release/"

    params {
        password("env.KEYSTORE_PASSWORD", "", label = "Keystore Password", display = ParameterDisplay.HIDDEN)
        password("env.KEY_PASSWORD", "", label = "Key Password", display = ParameterDisplay.HIDDEN)
        password("env.KEYSTORE_BASE64", "", label = "Keystore (base64)", display = ParameterDisplay.HIDDEN)
        param("env.KEY_ALIAS", "chessgym-upload")
        param("env.KEYSTORE_PATH", "%system.teamcity.build.checkoutDir%/chessgym-upload.keystore")
    }

    vcs {
        root(jetbrains.buildServer.configs.kotlin.AbsoluteId("ChessGym_GitHub"))
    }

    // No VCS trigger — release builds are triggered manually

    dependencies {
        snapshot(BuildDebug) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    decodeGoogleServicesJson()

    steps {
        powerShell {
            name = "Decode Keystore"
            scriptMode = script {
                content = """
                    ${'$'}ErrorActionPreference = "Stop"
                    ${'$'}bytes = [System.Convert]::FromBase64String(${'$'}env:KEYSTORE_BASE64)
                    [System.IO.File]::WriteAllBytes("chessgym-upload.keystore", ${'$'}bytes)
                    Write-Host "Keystore decoded"
                """.trimIndent()
            }
        }
        gradle {
            name = "Bundle Release"
            tasks = "clean :app:bundleRelease"
            useGradleWrapper = true
            gradleParams = "-PbuildNumber=%build.counter%"
        }
    }

    failureConditions {
        errorMessage = true
        nonZeroExitCode = true
    }
})
