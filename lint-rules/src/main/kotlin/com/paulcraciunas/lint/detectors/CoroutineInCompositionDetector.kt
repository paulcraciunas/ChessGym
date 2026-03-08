package com.paulcraciunas.lint.detectors

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod

internal class CoroutineInCompositionDetector : Detector(), SourceCodeScanner {
    override fun getApplicableMethodNames(): List<String> =
        listOf("launch", "async")

    override fun visitMethodCall(
        context: JavaContext,
        node: UCallExpression,
        method: PsiMethod,
    ) {
        if (!isDirectlyInComposableBody(node)) return
        context.report(ISSUE, node, context.getLocation(node), MESSAGE)
    }

    companion object {
        private const val MESSAGE =
            "Coroutine `launch`/`async` should not be called directly during composition. " +
                "Use `LaunchedEffect` or call from an event handler via `rememberCoroutineScope`."

        private const val COMPOSABLE_ANNOTATION = "androidx.compose.runtime.Composable"

        val ISSUE: Issue = Issue.create(
            id = "CoroutineCreationInComposition",
            briefDescription = "Coroutine created during composition",
            explanation = "Calling `launch` or `async` directly in a @Composable function body " +
                "will create a new coroutine on every recomposition. " +
                "Use `LaunchedEffect` for side effects or `rememberCoroutineScope` " +
                "for event-driven coroutines.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                CoroutineInCompositionDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        private fun isDirectlyInComposableBody(node: UElement): Boolean {
            var parent = node.uastParent
            while (parent != null) {
                if (parent is ULambdaExpression) return false
                if (parent is UMethod) {
                    return parent.uAnnotations.any {
                        it.qualifiedName == COMPOSABLE_ANNOTATION
                    }
                }
                parent = parent.uastParent
            }
            return false
        }
    }
}
