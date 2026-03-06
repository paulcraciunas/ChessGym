import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.version

version = "2025.11"

project {
    description = "ChessGym Android Application CI"

    buildType {
        id("BuildDebug")
        name = "Build Debug"
        description = "Assembles the ChessGym debug APK"

        artifactRules = "app/build/outputs/apk/debug/*.apk => apk"

        vcs {
            root(AbsoluteId("ChessGymGitHub"))
        }

        steps {
            gradle {
                name = "Assemble Debug APK"
                tasks = "assembleDebug"
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
    }
}
