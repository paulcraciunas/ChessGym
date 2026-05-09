package buildTypes

import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.triggers.vcs

fun BuildType.applyCommonConfiguration() {
    vcs {
        root(AbsoluteId("ChessGym_GitHub"))
    }

    triggers {
        vcs {
            // +:* matches all branches (including master) to ensure the integrated code is always verified
            // -:pull/* prevents duplicate builds when a PR is opened (the feature branch build is sufficient)
            branchFilter = """
                +:*
                -:pull/*
            """.trimIndent()
            // We have a separate CI for the backend
            triggerRules = """
                -:backend/**
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

/**
 * Decodes google-services.json from the GOOGLE_SERVICES_JSON env variable (base64-encoded).
 * Must be called before any Gradle step that compiles the :app module.
 * Not needed for the uitest variant which has a committed google-services.json.
 */
fun BuildType.decodeGoogleServicesJson() {
    steps {
        powerShell {
            name = "Decode google-services.json"
            scriptMode = script {
                content = """
                    ${'$'}ErrorActionPreference = "Stop"
                    if (${'$'}env:GOOGLE_SERVICES_JSON) {
                        ${'$'}bytes = [System.Convert]::FromBase64String(${'$'}env:GOOGLE_SERVICES_JSON)
                        [System.IO.File]::WriteAllBytes("app/google-services.json", ${'$'}bytes)
                        Write-Host "google-services.json written from CI secret"
                    } elseif (Test-Path "app/google-services.json") {
                        Write-Host "google-services.json already present"
                    } else {
                        Write-Error "google-services.json not found and GOOGLE_SERVICES_JSON not set"
                        exit 1
                    }
                """.trimIndent()
            }
        }
    }
}
