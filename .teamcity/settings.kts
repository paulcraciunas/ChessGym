import jetbrains.buildServer.configs.kotlin.ParameterDisplay
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.version

import buildTypes.BuildDebug
import buildTypes.UnitTests

version = "2025.11"

project {
    description = "ChessGym Android Application CI"

    params {
        password("github.token", "", label = "GitHub Personal Access Token", display = ParameterDisplay.HIDDEN)
    }

    buildType(BuildDebug)
    buildType(UnitTests)
}
