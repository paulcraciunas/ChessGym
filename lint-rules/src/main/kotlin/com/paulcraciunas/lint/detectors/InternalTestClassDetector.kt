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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

internal class InternalTestClassDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitClass(node: UClass) {
            if (!containsTestMethods(node)) return
            if (!isInternal(node)) {
                context.report(ISSUE, node, context.getNameLocation(node), MESSAGE)
            }
        }
    }

    companion object {
        private const val MESSAGE = "Test classes must be marked as `internal`."

        private val TEST_ANNOTATIONS = setOf(
            "org.junit.Test",
            "org.junit.jupiter.api.Test",
        )

        val ISSUE: Issue = Issue.create(
            id = "InternalTestClass",
            briefDescription = "Test class is not marked internal",
            explanation = "All test classes should be marked with the `internal` visibility " +
                "modifier to prevent accidental usage outside of the test module.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                InternalTestClassDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        private fun containsTestMethods(clazz: UClass): Boolean =
            clazz.methods.any { method ->
                method.uAnnotations.any { it.qualifiedName in TEST_ANNOTATIONS }
            }

        private fun isInternal(clazz: UClass): Boolean {
            val ktClass = clazz.sourcePsi as? KtClassOrObject ?: return true
            return ktClass.hasModifier(KtTokens.INTERNAL_KEYWORD)
        }
    }
}
