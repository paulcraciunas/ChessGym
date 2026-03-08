package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

internal class DisallowedImportDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = DisallowedImportDetector()
    override fun getIssues(): List<Issue> = listOf(DisallowedImportDetector.ISSUE)

    fun testMockkImport_error() {
        lint().files(
            kotlin(
                """
                package test
                import io.mockk.mockk
                internal class MyTest
                """
            ).indented()
        ).allowMissingSdk().allowCompilationErrors().run().expectErrorCount(1)
    }

    fun testMockitoImport_error() {
        lint().files(
            kotlin(
                """
                package test
                import org.mockito.Mockito
                internal class MyTest
                """
            ).indented()
        ).allowMissingSdk().allowCompilationErrors().run().expectErrorCount(1)
    }

    fun testRxJavaImport_error() {
        lint().files(
            kotlin(
                """
                package test
                import io.reactivex.Observable
                internal class MyTest
                """
            ).indented()
        ).allowMissingSdk().allowCompilationErrors().run().expectErrorCount(1)
    }

    fun testAllowedImport_noError() {
        lint().files(
            composableStub,
            kotlin(
                """
                package test
                import androidx.compose.runtime.Composable
                internal class MyTest {
                    @Composable
                    fun helper() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testMultipleMockImports_multipleErrors() {
        lint().files(
            kotlin(
                """
                package test
                import io.mockk.mockk
                import io.mockk.every
                import org.mockito.Mockito
                internal class MyTest
                """
            ).indented()
        ).allowMissingSdk().allowCompilationErrors().run().expectErrorCount(3)
    }

    fun testNoImports_noError() {
        lint().files(
            kotlin(
                """
                package test
                internal class MyTest {
                    fun helper() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }
}
