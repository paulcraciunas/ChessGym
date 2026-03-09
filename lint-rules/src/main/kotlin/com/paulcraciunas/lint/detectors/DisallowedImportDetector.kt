package com.paulcraciunas.lint.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.client.api.UElementHandler
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement

internal class DisallowedImportDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UImportStatement::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitImportStatement(node: UImportStatement) {
            val importRef = node.importReference?.asSourceString() ?: return
            val matchedLibrary = findDisallowedLibrary(importRef) ?: return
            context.report(
                ISSUE,
                node,
                context.getLocation(node),
                "Import from disallowed library: $matchedLibrary. ${ALTERNATIVES[matchedLibrary] ?: ""}",
            )
        }
    }

    companion object {
        private val DISALLOWED_PACKAGES = mapOf(
            "io.mockk" to "mockk",
            "org.mockito" to "mockito",
            "io.reactivex" to "rxjava"
        )

        private val ALTERNATIVES = mapOf(
            "mockk" to "Use fakes instead of mocks for test doubles.",
            "mockito" to "Use fakes instead of mocks for test doubles.",
            "rxjava" to "Use Coroutines instead of RxJava for asynchronous programming"
        )

        val ISSUE: Issue = Issue.create(
            id = "DisallowedImport",
            briefDescription = "Import from a disallowed library",
            explanation = "Certain libraries are disallowed in this project. " +
                "Currently blacklisted: mockito, mockk, rxjava. " +
                "Use fakes to simulate dependencies instead of mocking frameworks and coroutines instead of RxJava.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                DisallowedImportDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        private fun findDisallowedLibrary(importPath: String): String? {
            for ((packagePrefix, libraryName) in DISALLOWED_PACKAGES) {
                if (importPath.startsWith(packagePrefix)) return libraryName
            }
            return null
        }
    }
}
