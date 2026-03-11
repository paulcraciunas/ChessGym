package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.home.ui.HomeScreenTags

class HomeScreenAssertions(private val rule: ComposeTestRule) {
    fun isDisplayed(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.SCREEN).assertIsDisplayed()
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

    fun hasHighScoresCard(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.HIGH_SCORES_CARD).assertIsDisplayed()
    }

    fun hasTimeline(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Timeline.ROOT).assertIsDisplayed()
    }

    fun hasEmptyTimeline(): HomeScreenAssertions = apply {
        rule.onNodeWithTag(HomeScreenTags.Timeline.EMPTY).assertIsDisplayed()
    }
}
