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
include(":global:device:api")
include(":global:device:di")
include(":global:device:impl")
include(":global:notifications")
include(":global:resources")
include(":screens:common")
include(":screens:home:ui")
include(":screens:home:vm")
include(":screens:loading:ui")
include(":screens:loading:vm")
include(":settings:application")
include(":settings:testFixtures")
include(":settings:user")