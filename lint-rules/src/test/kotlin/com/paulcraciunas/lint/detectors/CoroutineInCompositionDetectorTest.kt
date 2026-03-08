package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

internal class CoroutineInCompositionDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = CoroutineInCompositionDetector()
    override fun getIssues(): List<Issue> = listOf(CoroutineInCompositionDetector.ISSUE)

    fun testLaunchInComposable_directBody_error() {
        lint().files(
            composableStub,
            coroutineScopeStub,
            kotlin(
                """
                package com.example
                import androidx.compose.runtime.Composable
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.launch

                @Composable
                fun MyComposable(scope: CoroutineScope) {
                    scope.launch { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testLaunchInsideLambda_noError() {
        lint().files(
            composableStub,
            coroutineScopeStub,
            kotlin(
                """
                package com.example
                import androidx.compose.runtime.Composable
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.launch

                @Composable
                fun MyComposable(scope: CoroutineScope, onClick: (() -> Unit) -> Unit) {
                    onClick { scope.launch { } }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testNonComposableFunction_noError() {
        lint().files(
            coroutineScopeStub,
            kotlin(
                """
                package com.example
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.launch

                fun regularFunction(scope: CoroutineScope) {
                    scope.launch { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }
}
