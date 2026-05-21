package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.paulcraciunas.screens.signin.ui.SignInTags

class SignInScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): SignInScreenAssertions = apply {
        rule.onNodeWithTag(SignInTags.EMAIL_FIELD).assertIsDisplayed()
        rule.onNodeWithTag(SignInTags.PASSWORD_FIELD).assertIsDisplayed()
        rule.onNodeWithTag(SignInTags.SUBMIT_BUTTON).assertIsDisplayed()
    }

    fun displayNameFieldIsVisible(): SignInScreenAssertions = apply {
        rule.onNodeWithTag(SignInTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
    }

    fun displayNameFieldIsNotVisible(): SignInScreenAssertions = apply {
        rule.onNodeWithTag(SignInTags.DISPLAY_NAME_FIELD).assertDoesNotExist()
    }

    fun forgotPasswordIsVisible(): SignInScreenAssertions = apply {
        rule.onNodeWithTag(SignInTags.FORGOT_PASSWORD_BUTTON).assertIsDisplayed()
    }

    fun forgotPasswordIsNotVisible(): SignInScreenAssertions = apply {
        rule.onNodeWithTag(SignInTags.FORGOT_PASSWORD_BUTTON).assertDoesNotExist()
    }

    fun hasEmailFieldError(errorText: String): SignInScreenAssertions = apply {
        rule.onNode(hasText(errorText)).assertIsDisplayed()
    }

    fun hasPasswordFieldError(errorText: String): SignInScreenAssertions = apply {
        rule.onNode(hasText(errorText)).assertIsDisplayed()
    }

    fun hasEmailAndPasswordError(errorText: String): SignInScreenAssertions = apply {
        rule.onAllNodes(hasText(errorText)).assertCountEquals(2)
    }

    fun hasDisplayNameFieldError(errorText: String): SignInScreenAssertions = apply {
        rule.onNode(hasText(errorText)).assertIsDisplayed()
    }

    fun hasNoGeneralError(): SignInScreenAssertions = apply {
        rule.onNodeWithTag(SignInTags.GENERAL_ERROR_BANNER).assertDoesNotExist()
    }

    fun passwordResetBannerIsVisible(): SignInScreenAssertions = apply {
        rule.onNodeWithTag(SignInTags.PASSWORD_RESET_BANNER).assertIsDisplayed()
    }
}
