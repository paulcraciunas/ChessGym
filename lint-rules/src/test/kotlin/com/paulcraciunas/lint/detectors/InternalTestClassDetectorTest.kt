package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

internal class InternalTestClassDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = InternalTestClassDetector()
    override fun getIssues(): List<Issue> = listOf(InternalTestClassDetector.ISSUE)

    fun testInternalTestClass_noError() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test
                internal class MyTest {
                    @Test
                    fun `WHEN action THEN result`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testPublicTestClass_error() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test
                class MyTest {
                    @Test
                    fun `WHEN action THEN result`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testPrivateTestClass_error() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test
                private class MyTest {
                    @Test
                    fun `WHEN action THEN result`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testClassWithoutTests_noError() {
        lint().files(
            kotlin(
                """
                package test
                class NotATestClass {
                    fun helper() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testJunit4InternalTestClass_noError() {
        lint().files(
            junit4TestStub,
            kotlin(
                """
                package test
                import org.junit.Test
                internal class MyJunit4Test {
                    @Test
                    fun `WHEN action THEN result`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testJunit4PublicTestClass_error() {
        lint().files(
            junit4TestStub,
            kotlin(
                """
                package test
                import org.junit.Test
                class MyJunit4Test {
                    @Test
                    fun `WHEN action THEN result`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }
}
