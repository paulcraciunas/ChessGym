package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.paulcraciunas.screens.signin.ui.SignInTags

class SignInScreenActions(private val rule: ComposeTestRule) {

    fun typeDisplayName(name: String): SignInScreenActions = apply {
        rule.onNodeWithTag(SignInTags.DISPLAY_NAME_FIELD)
            .performTextClearance()
        rule.onNodeWithTag(SignInTags.DISPLAY_NAME_FIELD)
            .performTextInput(name)
        rule.waitForIdle()
    }

    fun typeEmail(email: String): SignInScreenActions = apply {
        rule.onNodeWithTag(SignInTags.EMAIL_FIELD)
            .performTextClearance()
        rule.onNodeWithTag(SignInTags.EMAIL_FIELD)
            .performTextInput(email)
        rule.waitForIdle()
    }

    fun typePassword(password: String): SignInScreenActions = apply {
        rule.onNodeWithTag(SignInTags.PASSWORD_FIELD)
            .performTextClearance()
        rule.onNodeWithTag(SignInTags.PASSWORD_FIELD)
            .performTextInput(password)
        rule.waitForIdle()
    }

    fun tapSubmit(): SignInScreenActions = apply {
        rule.onNodeWithTag(SignInTags.SUBMIT_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun toggleToSignUp(): SignInScreenActions = apply {
        rule.onNodeWithTag(SignInTags.TOGGLE_MODE_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun toggleToSignIn(): SignInScreenActions = apply {
        rule.onNodeWithTag(SignInTags.TOGGLE_MODE_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun tapForgotPassword(): SignInScreenActions = apply {
        rule.onNodeWithTag(SignInTags.FORGOT_PASSWORD_BUTTON).performClick()
        rule.waitForIdle()
    }
}
