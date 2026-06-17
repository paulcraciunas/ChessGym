package com.paulcraciunas.chessgym.base

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import com.paulcraciunas.chessgym.dsl.Given
import com.paulcraciunas.chessgym.dsl.Then
import com.paulcraciunas.chessgym.dsl.When
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule

@Suppress("PropertyName")
@HiltAndroidTest
abstract class BaseUiTest {
    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    protected val Given = Given()
    protected val When = When(composeRule)
    protected val Then = Then(composeRule)

    @Before
    open fun setUp() {
        hiltRule.inject()
        Given.puzzle.reset()
    }

    @After
    open fun tearDown() {
        When.app.close()
    }
}
