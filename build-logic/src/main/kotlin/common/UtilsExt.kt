package common

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

internal inline fun <reified T> Project.extension(name: String): T =
    extensions.create(name, T::class.java)

internal fun Project.configureJava(jvm: ProjectConfig.Jvm) {
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(jvm.version))
        }
    }
}

internal fun Project.configureKotlin(jvm: ProjectConfig.Jvm) {
    kotlinExtension.apply {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(jvm.version))
        }
    }
}
