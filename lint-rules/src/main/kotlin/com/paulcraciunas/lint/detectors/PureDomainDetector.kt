package com.paulcraciunas.lint.detectors

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement

internal class PureDomainDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UImportStatement::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitImportStatement(node: UImportStatement) {
            if (!isTargetModule(context)) return
            if (isTestSource(context)) return
            val importRef = node.importReference?.asSourceString() ?: return
            if (isAndroidImport(importRef)) {
                context.report(ISSUE, node, context.getLocation(node), MESSAGE)
            }
        }
    }

    companion object {
        private const val MESSAGE =
            "Domain and game modules must remain pure Kotlin. " +
                "Android framework imports are forbidden in production sources."

        private val FORBIDDEN_PREFIXES = listOf(
            "android.",
            "androidx.",
        )

        private val SEP = java.io.File.separatorChar

        private val TARGET_MODULE_NAMES = listOf("domain", "game")

        private val TEST_SOURCE_INDICATORS = listOf(
            "${SEP}src${SEP}test${SEP}",
            "${SEP}src${SEP}androidTest${SEP}",
            "${SEP}src${SEP}testFixtures${SEP}",
        )

        val ISSUE: Issue = Issue.create(
            id = "PureDomain",
            briefDescription = "Android framework import in domain/game module",
            explanation = "Domain and game modules must not depend on the Android framework " +
                "in production sources. Use pure Kotlin abstractions and inject " +
                "platform-specific implementations via dependency inversion. " +
                "Test sources are exempt from this rule.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                PureDomainDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        private fun isTargetModule(context: JavaContext): Boolean {
            val projectDir = context.project.dir.path
            return TARGET_MODULE_NAMES.any { name ->
                projectDir.contains("${SEP}$name${SEP}") ||
                    projectDir.endsWith("${SEP}$name")
            }
        }

        private fun isTestSource(context: JavaContext): Boolean {
            val filePath = context.file.path
            return TEST_SOURCE_INDICATORS.any { filePath.contains(it) }
        }

        private fun isAndroidImport(importPath: String): Boolean =
            FORBIDDEN_PREFIXES.any { importPath.startsWith(it) }
    }
}
