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
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile

internal class TodoTicketDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UFile::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitFile(node: UFile) {
            node.sourcePsi.accept(object : PsiRecursiveElementVisitor() {
                override fun visitComment(comment: PsiComment) {
                    val text = comment.text
                    if (containsTodo(text) && !hasTicketReference(text)) {
                        context.report(ISSUE, comment, context.getLocation(comment), MESSAGE)
                    }
                }
            })
        }
    }

    companion object {
        private const val ISSUES_URL = "https://github.com/paulcraciunas/ChessGym/issues"

        private const val MESSAGE =
            "TODO comments must include a link to a GitHub issue. " +
                "Use format: TODO($ISSUES_URL/42) Explanation"

        private val TODO_PATTERN = Regex("""TODO\b""", RegexOption.IGNORE_CASE)
        private val TICKET_PATTERN = Regex(
            """TODO\s*\(\s*https://github\.com/paulcraciunas/ChessGym/issues/\d+\s*\)""",
            RegexOption.IGNORE_CASE,
        )

        val ISSUE: Issue = Issue.create(
            id = "TodoWithoutTicket",
            briefDescription = "TODO without a GitHub issue link",
            explanation = "Every TODO comment must include a link to a GitHub issue " +
                "so it can be prioritized and resolved. " +
                "Use format: TODO($ISSUES_URL/42) Explanation",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(
                TodoTicketDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
        )

        internal fun containsTodo(text: String): Boolean =
            TODO_PATTERN.containsMatchIn(text)

        internal fun hasTicketReference(text: String): Boolean =
            TICKET_PATTERN.containsMatchIn(text)
    }
}
