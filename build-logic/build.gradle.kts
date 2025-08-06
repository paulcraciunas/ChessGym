import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.tools.build.gradle)
    compileOnly(libs.google.ksp.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("libraryConvention") {
            id = "conventions.library"
            implementationClass = "plugins.library.LibraryConventionPlugin"
        }
        register("androidLibraryConvention") {
            id = "conventions.android.library"
            implementationClass = "plugins.android.AndroidConventionPlugin"
        }
        register("androidAppConvention") {
            id = "conventions.android.app"
            implementationClass = "plugins.android.AndroidApplicationPlugin"
        }
    }
}
