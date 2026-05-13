package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.common.SemanticsKeys
import com.paulcraciunas.screens.common.theme.ThemeTags

class ThemeAssertions(private val rule: ComposeTestRule) {

    fun isDarkMode(): ThemeAssertions = apply {
        rule.onNodeWithTag(ThemeTags.ROOT).assert(
            SemanticsMatcher.expectValue(SemanticsKeys.IsDarkTheme, true)
        )
    }

    fun isLightMode(): ThemeAssertions = apply {
        rule.onNodeWithTag(ThemeTags.ROOT).assert(
            SemanticsMatcher.expectValue(SemanticsKeys.IsDarkTheme, false)
        )
    }
}
