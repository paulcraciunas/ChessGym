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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
include(":design")
include(":domain:api")
include(":domain:impl")
include(":domain:di")
include(":game:logic:api")
include(":game:logic:di")
include(":game:logic:impl")
include(":game:puzzles:api")
include(":game:puzzles:di")
include(":game:puzzles:impl")
include(":game:engine:api")
include(":game:engine:impl")
include(":game:serializer:api")
include(":game:serializer:di")
include(":game:serializer:impl")
include(":global:device:api")
include(":global:device:di")
include(":global:device:impl")
include(":global:extensions")
include(":global:qualifiers")
include(":global:notifications")
include(":global:resources")
include(":global:utils")
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
include(":screens:boardvis:dashboard:ui")
include(":screens:boardvis:dashboard:vm")
include(":screens:boardvis:squares:ui")
include(":screens:boardvis:squares:vm")
include(":screens:boardvis:pieces:ui")
include(":screens:boardvis:pieces:vm")
include(":screens:settings:ui")
include(":screens:settings:vm")
include(":screens:signin:ui")
include(":screens:signin:vm")
include(":screens:about:ui")
include(":screens:about:vm")
include(":screens:achievements:ui")
include(":screens:achievements:vm")
include(":screens:blindmode:ui")
include(":screens:blindmode:vm")
include(":screens:tools:dashboard:ui")
include(":screens:tools:dashboard:vm")
include(":screens:tools:clock:ui")
include(":screens:tools:clock:vm")
include(":screens:tools:analysis:ui")
include(":screens:tools:analysis:vm")
include(":screens:tools:importgame:ui")
include(":screens:tools:importgame:vm")
include(":settings:application:api")
include(":settings:application:impl")
include(":user:api")
include(":user:impl")
include(":user:remote")
include(":user:di")
include(":lint-rules")
include(":tools:puzzle-verifier")
