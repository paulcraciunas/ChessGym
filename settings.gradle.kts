@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ChessGym"
include(":app")
include(":game:logic:api")
include(":game:logic:di")
include(":game:logic:impl")
include(":game:puzzles:api")
include(":game:puzzles:di")
include(":game:puzzles:impl")
include(":game:serializer:api")
include(":game:serializer:impl")
include(":global:notifications")
include(":settings:application")
include(":settings:user")