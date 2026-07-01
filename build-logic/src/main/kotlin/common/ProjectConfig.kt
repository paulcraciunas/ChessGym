package common

interface ProjectConfig {
    val jvm: Jvm
    val android: Android
    val version: Version

    interface Jvm {
        val version: Int
    }

    interface Android {
        val compileSdk: Int
        val targetSdk: Int
        val minSdk: Int
    }

    interface Version {
        val major: Int
        val minor: Int
    }
}
