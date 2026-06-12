package com.paulcraciunas.chessgym.screens.auth

import com.paulcraciunas.chessgym.base.BaseUiTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class SignInScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings.puzzlesDownloaded()
        Given.user.isDefault()
    }

    @Test
    fun WHEN_navigated_to_sign_in_THEN_shows_sign_in_form() {
        When.app.launch()
        When.navigation.navigateToSignIn()

        Then.signInScreen
            .isDisplayed()
            .displayNameFieldIsNotVisible()
            .forgotPasswordIsVisible()
    }

    @Test
    fun WHEN_toggle_to_sign_up_THEN_shows_display_name_field() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen.toggleToSignUp()

        Then.signInScreen
            .isDisplayed()
            .displayNameFieldIsVisible()
            .forgotPasswordIsNotVisible()
    }

    @Test
    fun WHEN_toggle_to_sign_up_and_back_THEN_display_name_field_disappears() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen.toggleToSignUp()

        Then.signInScreen.displayNameFieldIsVisible()

        When.signInScreen.toggleToSignIn()

        Then.signInScreen
            .displayNameFieldIsNotVisible()
            .forgotPasswordIsVisible()
    }

    @Test
    fun GIVEN_sign_in_mode_WHEN_submit_with_empty_fields_THEN_shows_field_errors() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen.tapSubmit()

        Then.signInScreen
            .hasEmailAndPasswordError("This field is required.")
            .hasNoGeneralError()
    }

    @Test
    fun GIVEN_sign_in_mode_WHEN_submit_with_invalid_email_THEN_shows_email_error() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .typeEmail("not-an-email")
            .typePassword("ValidPass123")
            .tapSubmit()

        Then.signInScreen
            .hasEmailFieldError("Please enter a valid email address.")
    }

    @Test
    fun GIVEN_sign_in_mode_WHEN_submit_with_short_password_THEN_shows_password_error() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .typeEmail("user@example.com")
            .typePassword("123")
            .tapSubmit()

        Then.signInScreen
            .hasPasswordFieldError("Password must be at least 6 characters.")
    }

    @Test
    fun GIVEN_valid_credentials_WHEN_sign_in_THEN_navigates_back() {
        Given.auth.withExistingUser("user@example.com", "ValidPass123")

        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .typeEmail("user@example.com")
            .typePassword("ValidPass123")
            .tapSubmit()

        Then.homeScreen.isDisplayed()
    }

    @Test
    fun GIVEN_invalid_credentials_WHEN_sign_in_THEN_shows_password_error() {
        Given.auth.withExistingUser("user@example.com", "CorrectPass")

        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .typeEmail("user@example.com")
            .typePassword("WrongPass123")
            .tapSubmit()

        Then.signInScreen
            .hasPasswordFieldError("Incorrect password. Please try again.")
    }

    @Test
    fun GIVEN_non_existent_user_WHEN_sign_in_THEN_shows_email_error() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .typeEmail("unknown@example.com")
            .typePassword("ValidPass123")
            .tapSubmit()

        Then.signInScreen
            .hasEmailFieldError("No account found with this email.")
    }

    @Test
    fun GIVEN_sign_up_mode_WHEN_submit_with_empty_display_name_THEN_shows_error() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .toggleToSignUp()
            .typeEmail("user@example.com")
            .typePassword("ValidPass123")
            .tapSubmit()

        Then.signInScreen
            .hasDisplayNameFieldError("This field is required.")
    }

    @Test
    fun GIVEN_sign_up_mode_WHEN_submit_with_short_display_name_THEN_shows_error() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .toggleToSignUp()
            .typeDisplayName("A")
            .typeEmail("user@example.com")
            .typePassword("ValidPass123")
            .tapSubmit()

        Then.signInScreen
            .hasDisplayNameFieldError("Display name must be at least 4 characters.")
    }

    @Test
    fun GIVEN_sign_up_mode_WHEN_submit_with_valid_fields_THEN_navigates_back() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .toggleToSignUp()
            .typeDisplayName("ChessPlayer")
            .typeEmail("new@example.com")
            .typePassword("ValidPass123")
            .tapSubmit()

        Then.homeScreen.isDisplayed()
    }

    @Test
    fun GIVEN_existing_account_WHEN_sign_up_THEN_shows_email_error() {
        Given.auth.withExistingUser("taken@example.com", "SomePass123")

        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .toggleToSignUp()
            .typeDisplayName("ChessPlayer")
            .typeEmail("taken@example.com")
            .typePassword("SomePass123")
            .tapSubmit()

        Then.signInScreen
            .hasEmailFieldError("An account with this email already exists.")
    }

    @Test
    fun GIVEN_existing_user_WHEN_forgot_password_THEN_shows_reset_banner() {
        Given.auth.withExistingUser("user@example.com", "ForgottenPass")

        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .typeEmail("user@example.com")
            .tapForgotPassword()

        Then.signInScreen.passwordResetBannerIsVisible()
    }

    @Test
    fun GIVEN_empty_email_WHEN_forgot_password_THEN_shows_email_error() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen.tapForgotPassword()

        Then.signInScreen
            .hasEmailFieldError("This field is required.")
    }

    @Test
    fun GIVEN_unknown_email_WHEN_forgot_password_THEN_shows_email_error() {
        When.app.launch()
        When.navigation.navigateToSignIn()
        When.signInScreen
            .typeEmail("unknown@example.com")
            .tapForgotPassword()

        Then.signInScreen
            .hasEmailFieldError("No account found with this email address.")
    }
}
