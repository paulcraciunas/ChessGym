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
import org.jetbrains.uast.UMethod

internal class TestNamingDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UMethod::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitMethod(node: UMethod) {
            if (!hasTestAnnotation(node)) return
            if (!isValidTestName(node.name)) {
                context.report(ISSUE, node, context.getNameLocation(node), MESSAGE)
            }
        }
    }

    companion object {
        private const val MESSAGE =
            "Test methods must follow the GIVEN-WHEN-THEN naming convention. " +
                "GIVEN is optional, but WHEN and THEN are required and must appear in that order."

        private val TEST_ANNOTATIONS = setOf(
            "org.junit.Test",
            "org.junit.jupiter.api.Test",
        )

        val ISSUE: Issue = Issue.create(
            id = "TestNamingConvention",
            briefDescription = "Test method does not follow GIVEN-WHEN-THEN convention",
            explanation = "All test methods annotated with @Test must follow the " +
                "GIVEN-WHEN-THEN naming convention. The GIVEN clause is optional, " +
                "but WHEN and THEN must be present and in that order. " +
                "Example: `GIVEN logged in user WHEN profile clicked THEN show profile`",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                TestNamingDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        private fun hasTestAnnotation(method: UMethod): Boolean =
            method.uAnnotations.any { it.qualifiedName in TEST_ANNOTATIONS }

        internal fun isValidTestName(name: String): Boolean {
            val tokens = name
                .replace('`', ' ')
                .split(Regex("[\\s_]+"))
                .map { it.uppercase() }

            val whenIndex = tokens.indexOf("WHEN")
            val thenIndex = tokens.indexOf("THEN")
            if (whenIndex < 0 || thenIndex < 0) return false
            if (thenIndex <= whenIndex) return false

            val givenIndex = tokens.indexOf("GIVEN")
            if (givenIndex >= 0 && givenIndex > whenIndex) return false

            return true
        }
    }
}
