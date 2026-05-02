package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle

object BuildDebug : BuildType({
    id("BuildDebug")
    name = "Build Debug"
    description = "Assembles the ChessGym debug APK"

    artifactRules = "app/build/outputs/apk/debug/*.apk => apk"

    applyCommonConfiguration()
    decodeGoogleServicesJson()

    steps {
        gradle {
            name = "Assemble Debug APK"
            tasks = "clean assembleDebug"
            useGradleWrapper = true
            gradleParams = "--no-build-cache"
        }
    }

    failureConditions {
        errorMessage = true
        nonZeroExitCode = true
    }
})
