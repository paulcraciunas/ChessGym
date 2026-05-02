import jetbrains.buildServer.configs.kotlin.ParameterDisplay
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.version

import buildTypes.BuildDebug
import buildTypes.InstrumentedTests
import buildTypes.LintDebug
import buildTypes.PuzzleVerification
import buildTypes.UnitTests

version = "2025.11"

project {
    description = "ChessGym Android Application CI"

    params {
        password("github.token", "credentialsJSON:543085ac-69ea-4b55-a5b8-31a17c288563", label = "GitHub Personal Access Token", display = ParameterDisplay.HIDDEN)
        password("env.GOOGLE_SERVICES_JSON", "", label = "google-services.json (base64)", display = ParameterDisplay.HIDDEN)
    }

    buildType(BuildDebug)
    buildType(InstrumentedTests)
    buildType(LintDebug)
    buildType(UnitTests)
    buildType(PuzzleVerification)
}
