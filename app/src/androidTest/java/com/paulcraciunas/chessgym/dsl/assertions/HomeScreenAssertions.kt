package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.common.SemanticsKeys
import com.paulcraciunas.screens.home.ui.HomeScreenTags

class HomeScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.SCREEN).assertIsDisplayed()
    }

    fun hasBackgroundColor(color: Color): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.SCREEN).assert(
            SemanticsMatcher.expectValue(SemanticsKeys.BackgroundColor, color)
        )
    }

    fun hasProfileName(name: String): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Profile.NAME).assertTextEquals(name)
    }

    fun hasProfileInitials(initials: String): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Profile.INITIALS).assertTextEquals(initials)
    }

    fun hasProfileRating(rating: Int): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Profile.RATING).assertTextEquals(rating.toString())
    }

    fun hasProfileInfo(name: String, initials: String, rating: Int): HomeScreenAssertions = apply {
        hasProfileName(name)
        hasProfileInitials(initials)
        hasProfileRating(rating)
    }

    fun hasProfileCard(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Profile.CARD).assertIsDisplayed()
    }

    fun hasStatsCard(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.STATS_CARD).assertIsDisplayed()
    }

    fun hasStatsExpanded(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.STATS_EXPANDED_CONTENT).assertIsDisplayed()
    }

    fun hasStatsCollapsed(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.STATS_EXPANDED_CONTENT).assertIsNotDisplayed()
    }

    fun hasHighScoresCard(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.HIGH_SCORES_CARD).assertIsDisplayed()
    }

    fun hasHighScoresExpanded(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.HIGH_SCORES_EXPANDED_CONTENT).assertIsDisplayed()
    }

    fun hasHighScoresCollapsed(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.HIGH_SCORES_EXPANDED_CONTENT).assertIsNotDisplayed()
    }

    fun hasTimeline(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Timeline.ROOT).assertIsDisplayed()
    }

    fun hasEmptyTimeline(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Timeline.EMPTY).assertIsDisplayed()
    }
}
