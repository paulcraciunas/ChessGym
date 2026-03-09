package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.ProjectDescription
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

internal class PureDomainDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = PureDomainDetector()
    override fun getIssues(): List<Issue> = listOf(PureDomainDetector.ISSUE)

    fun testPureKotlinImport_inDomainModule_noError() {
        val project = ProjectDescription()
            .name("domain")
            .files(
                flowStub,
                kotlin(
                    """
                    package com.paulcraciunas.domain
                    import kotlinx.coroutines.flow.Flow
                    interface UseCase {
                        fun execute(): Flow<Unit>
                    }
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    fun testAndroidImport_inDomainModule_error() {
        val project = ProjectDescription()
            .name("domain")
            .files(
                kotlin(
                    """
                    package com.paulcraciunas.domain
                    import android.content.Context
                    class PlatformHelper(private val context: Context)
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .allowCompilationErrors()
            .run()
            .expectErrorCount(1)
    }

    fun testAndroidImport_inDomainChildModule_error() {
        val project = ProjectDescription()
            .name("domain")
            .files(
                kotlin(
                    """
                    package com.paulcraciunas.domain.api
                    import android.content.Context
                    class PlatformHelper(private val context: Context)
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .allowCompilationErrors()
            .run()
            .expectErrorCount(1)
    }

    fun testAndroidImport_inGameModule_error() {
        val project = ProjectDescription()
            .name("game")
            .files(
                kotlin(
                    """
                    package com.paulcraciunas.game
                    import androidx.lifecycle.ViewModel
                    class GameViewModel : ViewModel()
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .allowCompilationErrors()
            .run()
            .expectErrorCount(1)
    }

    fun testAndroidImport_inGameChildModule_error() {
        val project = ProjectDescription()
            .name("game")
            .files(
                kotlin(
                    """
                    package com.paulcraciunas.game.logic
                    import androidx.lifecycle.ViewModel
                    class GameViewModel : ViewModel()
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .allowCompilationErrors()
            .run()
            .expectErrorCount(1)
    }

    fun testAndroidImport_outsideDomainOrGame_noError() {
        val project = ProjectDescription()
            .name("screens")
            .files(
                kotlin(
                    """
                    package com.paulcraciunas.screens
                    import android.content.Context
                    class ScreenHelper(private val context: Context)
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .allowCompilationErrors()
            .run()
            .expectClean()
    }

    fun testAndroidImport_inDomainTestSource_noError() {
        val project = ProjectDescription()
            .name("domain")
            .files(
                kotlin(
                    "src/test/java/com/paulcraciunas/domain/WorkManagerTest.kt",
                    """
                    package com.paulcraciunas.domain
                    import android.content.Context
                    class WorkManagerTest
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .allowCompilationErrors()
            .run()
            .expectClean()
    }

    fun testAndroidImport_inDomainAndroidTestSource_noError() {
        val project = ProjectDescription()
            .name("domain")
            .files(
                kotlin(
                    "src/androidTest/java/com/paulcraciunas/domain/InstrumentedTest.kt",
                    """
                    package com.paulcraciunas.domain
                    import androidx.test.ext.junit.runners.AndroidJUnit4
                    class InstrumentedTest
                    """
                ).indented()
            )
        lint().projects(project)
            .allowMissingSdk()
            .allowCompilationErrors()
            .run()
            .expectClean()
    }
}
