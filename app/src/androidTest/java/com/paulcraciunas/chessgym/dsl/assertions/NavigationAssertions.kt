package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.global.resources.R

class NavigationAssertions(private val rule: ComposeTestRule) {

    fun drawerIsOpen(): NavigationAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithText(context.getString(R.string.nav_drawer_settings))
            .assertIsDisplayed()
    }
}
