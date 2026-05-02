package buildTypes

import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.script
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
 * Only works on agents with bash (Linux/macOS). Not needed for the uitest variant which
 * has a committed google-services.json.
 */
fun BuildType.decodeGoogleServicesJson() {
    steps {
        script {
            name = "Decode google-services.json"
            scriptContent = """
                #!/usr/bin/env bash
                set -euo pipefail
                if [ -n "${'$'}{GOOGLE_SERVICES_JSON:-}" ]; then
                    echo "${'$'}GOOGLE_SERVICES_JSON" | base64 --decode > app/google-services.json
                    echo "google-services.json written from CI secret"
                elif [ -f app/google-services.json ]; then
                    echo "google-services.json already present"
                else
                    echo "ERROR: google-services.json not found and GOOGLE_SERVICES_JSON not set"
                    exit 1
                fi
            """.trimIndent()
            param("script.content.interpreterMode", "bash")
        }
    }
}
