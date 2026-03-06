package buildTypes

import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.triggers.vcs

object UnitTests : BuildType({
    id("UnitTests")
    name = "Unit Tests"
    description = "Runs all unit tests across all modules (debug variant)"

    vcs {
        root(AbsoluteId("ChessGym_GitHub"))
    }

    steps {
        gradle {
            name = "Run Unit Tests"
            tasks = "unitTestAllDebug"
            useGradleWrapper = true
            gradleWrapperPath = ""
        }
    }

    triggers {
        vcs {
            branchFilter = """
                +:*
                -:refs/heads/master
            """.trimIndent()
        }
    }

    features {
        pullRequests {
            provider = github {
                authType = token {
                    token = "%github.token%"
                }
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
            }
        }
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%github.token%"
                }
            }
        }
    }
})
