package buildTypes

import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.triggers.vcs

fun BuildType.applyCommonConfiguration() {
    vcs {
        root(AbsoluteId("ChessGym_GitHub"))
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
}
