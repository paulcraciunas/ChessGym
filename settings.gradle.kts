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

includeBuild("build-logic")

rootProject.name = "ChessGym"
include(":app")
include(":game:logic:api")
include(":game:logic:di")
include(":game:logic:impl")
include(":game:puzzles:api")
include(":game:puzzles:di")
include(":game:puzzles:impl")
include(":game:serializer:api")
include(":game:serializer:di")
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
include(":screens:puzzles:dashboard:ui")
include(":screens:puzzles:dashboard:vm")
include(":screens:puzzles:rated:ui")
include(":screens:puzzles:rated:vm")
include(":screens:puzzles:rush:ui")
include(":screens:puzzles:rush:vm")
include(":screens:puzzles:failed:ui")
include(":screens:puzzles:failed:vm")
include(":screens:puzzles:streak:ui")
include(":screens:puzzles:streak:vm")
include(":settings:application:api")
include(":settings:application:impl")
include(":domain:api")
include(":domain:impl")
include(":domain:di")
include(":user:api")
include(":user:impl")
include(":user:remote")
include(":user:di")
