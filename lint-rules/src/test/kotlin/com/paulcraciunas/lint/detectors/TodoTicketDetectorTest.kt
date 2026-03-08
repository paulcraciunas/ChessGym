package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

internal class TodoTicketDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = TodoTicketDetector()
    override fun getIssues(): List<Issue> = listOf(TodoTicketDetector.ISSUE)

    fun testTodoWithGitHubIssueLink_noError() {
        lint().files(
            kotlin(
                """
                package com.example
                // TODO(https://github.com/paulcraciunas/ChessGym/issues/42) Fix this later
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testTodoWithGitHubIssueLink_singleDigit_noError() {
        lint().files(
            kotlin(
                """
                package com.example
                // TODO(https://github.com/paulcraciunas/ChessGym/issues/1) First issue
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testTodoWithBareTicketNumber_error() {
        lint().files(
            kotlin(
                """
                package com.example
                // TODO(#123) Fix this later
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testTodoWithProjectKey_error() {
        lint().files(
            kotlin(
                """
                package com.example
                // TODO(CHESS-456) Implement feature
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testTodoWithoutAnyTicket_error() {
        lint().files(
            kotlin(
                """
                package com.example
                // TODO Fix this later
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testTodoWithoutTicket_blockComment_error() {
        lint().files(
            kotlin(
                """
                package com.example
                /* TODO: refactor this */
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testNoTodoComment_noError() {
        lint().files(
            kotlin(
                """
                package com.example
                // This is a regular comment
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testMultipleTodos_mixedValidity() {
        lint().files(
            kotlin(
                """
                package com.example
                // TODO(https://github.com/paulcraciunas/ChessGym/issues/1) Valid link
                // TODO invalid no ticket
                class MyClass
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }
}
