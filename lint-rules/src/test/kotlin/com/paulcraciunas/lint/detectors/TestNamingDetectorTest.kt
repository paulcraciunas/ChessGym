package com.paulcraciunas.lint.detectors

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

@Suppress("FunctionName") // For unit test methods which start with "GIVEN ..."
internal class TestNamingDetectorTest : LintDetectorTest() {
    override fun getDetector(): Detector = TestNamingDetector()
    override fun getIssues(): List<Issue> = listOf(TestNamingDetector.ISSUE)

    fun testValidGivenWhenThen_backtickStyle() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test

                internal class MyTest {
                    @Test
                    fun `GIVEN a user WHEN login THEN success`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testValidWhenThen_backtickStyle() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test

                internal class MyTest {
                    @Test
                    fun `WHEN login THEN success`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testValidSnakeCase() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test

                internal class MyTest {
                    @Test
                    fun given_a_user_when_login_then_success() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testValidSnakeCaseWithoutGiven() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test

                internal class MyTest {
                    @Test
                    fun when_login_then_success() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testInvalid_missingWhenAndThen() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test
                internal class MyTest {
                    @Test
                    fun testSomething() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testInvalid_missingThen() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test
                internal class MyTest {
                    @Test
                    fun `WHEN login succeeds`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testInvalid_wrongOrder() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test
                internal class MyTest {
                    @Test
                    fun `THEN success WHEN login`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testInvalid_givenAfterWhen() {
        lint().files(
            junit5TestStub,
            kotlin(
                """
                package test
                import org.junit.jupiter.api.Test
                internal class MyTest {
                    @Test
                    fun `WHEN login GIVEN user THEN success`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectErrorCount(1)
    }

    fun testNonTestMethod_noAnnotation() {
        lint().files(
            kotlin(
                """
                package test
                internal class MyTest {
                    fun helperMethod() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }

    fun testJunit4Annotation() {
        lint().files(
            junit4TestStub,
            kotlin(
                """
                package test
                import org.junit.Test
                internal class MyTest {
                    @Test
                    fun `WHEN action THEN result`() { }
                }
                """
            ).indented()
        ).allowMissingSdk().run().expectClean()
    }
}
