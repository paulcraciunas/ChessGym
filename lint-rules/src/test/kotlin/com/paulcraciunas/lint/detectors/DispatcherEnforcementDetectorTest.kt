package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

internal class DispatcherEnforcementDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = DispatcherEnforcementDetector()
    override fun getIssues(): List<Issue> = listOf(DispatcherEnforcementDetector.ISSUE)

    fun testInjectedDispatcher_noError() {
        lint().files(
            coroutineDispatcherStub,
            kotlin(
                """
                package com.paulcraciunas.data
                import kotlinx.coroutines.CoroutineDispatcher

                class MyRepository(private val dispatcher: CoroutineDispatcher) {
                    @Suppress("RedundantSuspendModifier")
                    suspend fun execute() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testNonInjectedDispatcher_error() {
        lint().files(
            coroutineDispatcherStub,
            coroutineWithContextStub,
            kotlin(
                """
                package com.paulcraciunas.data
                import kotlinx.coroutines.Dispatchers

                class MyRepository {
                    suspend fun execute() { 
                        withContext(Dispatchers.IO) {
                            delay(42L)
                        }
                    }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testHardcodedDispatcherIO_error() {
        lint().files(
            coroutineDispatcherStub,
            kotlin(
                """
                package com.paulcraciunas.data
                import kotlinx.coroutines.Dispatchers

                class MyRepository {
                    val dispatcher = Dispatchers.IO
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testHardcodedDispatcherDefault_error() {
        lint().files(
            coroutineDispatcherStub,
            kotlin(
                """
                package com.paulcraciunas.domain
                import kotlinx.coroutines.Dispatchers

                class MyUseCase {
                    val dispatcher = Dispatchers.Default
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testHardcodedDispatcherMain_error() {
        lint().files(
            coroutineDispatcherStub,
            kotlin(
                """
                package com.paulcraciunas.ui
                import kotlinx.coroutines.Dispatchers

                class MyViewModel {
                    val dispatcher = Dispatchers.Main
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testHardcodedDispatcherUnconfined_error() {
        lint().files(
            coroutineDispatcherStub,
            kotlin(
                """
                package com.paulcraciunas.util
                import kotlinx.coroutines.Dispatchers

                class CoroutineUtil {
                    val dispatcher = Dispatchers.Unconfined
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testDaggerModule_exempt() {
        lint().files(
            coroutineDispatcherStub,
            daggerModuleStub,
            kotlin(
                """
                package com.paulcraciunas.di
                import dagger.Module
                import kotlinx.coroutines.Dispatchers

                @Module
                class DispatchersModule {
                    fun provideIoDispatcher() = Dispatchers.IO
                    fun provideDefaultDispatcher() = Dispatchers.Default
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testNoDispatcherUsage_noError() {
        lint().files(
            kotlin(
                """
                package com.paulcraciunas.domain
                class MyUseCase {
                    @Suppress("RedundantSuspendModifier")
                    suspend fun execute() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }
}
