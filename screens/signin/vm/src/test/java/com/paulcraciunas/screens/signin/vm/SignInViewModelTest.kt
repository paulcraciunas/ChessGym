package com.paulcraciunas.screens.signin.vm

import com.paulcraciunas.domain.impl.auth.AuthenticateUseCaseImpl
import com.paulcraciunas.domain.impl.auth.ResetPasswordUseCaseImpl
import com.paulcraciunas.user.api.AuthException
import com.paulcraciunas.user.api.FakeAuthService
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SignInViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeAuthService = FakeAuthService()

    private lateinit var underTest: SignInViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = SignInViewModel(
            authenticateUseCase = AuthenticateUseCaseImpl(
                authService = fakeAuthService,
                userRepository = FakeUserRepository()
            ),
            resetPasswordUseCase = ResetPasswordUseCaseImpl(
                authService = fakeAuthService
            ),
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class GoogleSignIn {
        @Test
        fun `GIVEN successful flow WHEN onGoogleSignIn THEN state transitions to Success`() = runTest {
            fakeAuthService.withTokenUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            underTest.onGoogleSignIn { GOOGLE_TOKEN }
            advanceUntilIdle()

            assertTrue(underTest.uiState.value.isSuccess)
        }

        @Test
        fun `GIVEN unexpected exception WHEN onGoogleSignIn THEN state shows general error`() = runTest {
            fakeAuthService.withError(IllegalStateException("Unexpected"))
            fakeAuthService.withTokenUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            underTest.onGoogleSignIn { GOOGLE_TOKEN }
            advanceUntilIdle()

            assertEquals(AuthError.GOOGLE_SIGN_IN_FAILED, underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN no credentials WHEN onGoogleSignIn THEN state shows no google accounts error`() = runTest {
            underTest.onGoogleSignIn { throw AuthException.NoCredentials() }
            advanceUntilIdle()

            assertEquals(AuthError.NO_GOOGLE_ACCOUNTS, underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN network error WHEN onGoogleSignIn THEN state shows network error`() = runTest {
            fakeAuthService.disconnect()
            fakeAuthService.withTokenUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            underTest.onGoogleSignIn { GOOGLE_TOKEN }
            advanceUntilIdle()

            assertEquals(AuthError.NETWORK_ERROR, underTest.uiState.value.generalError)
        }
    }

    @Nested
    internal inner class EmailSignIn {
        @Test
        fun `GIVEN valid credentials WHEN onEmailSignIn THEN state transitions to Success`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertTrue(underTest.uiState.value.isSuccess)
        }

        @Test
        fun `GIVEN empty email WHEN onEmailSignIn THEN email field shows error`() = runTest {
            underTest.onEmailSignIn("", DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(AuthError.EMPTY_FIELD, underTest.uiState.value.fieldErrors.emailError)
            assertNull(underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN invalid email format WHEN onEmailSignIn THEN email field shows error`() = runTest {
            underTest.onEmailSignIn("not-an-email", DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(AuthError.INVALID_EMAIL, underTest.uiState.value.fieldErrors.emailError)
            assertNull(underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN short password WHEN onEmailSignIn THEN password field shows error`() = runTest {
            underTest.onEmailSignIn(DEFAULT_USER, "12345")
            advanceUntilIdle()

            assertEquals(AuthError.PASSWORD_TOO_SHORT, underTest.uiState.value.fieldErrors.passwordError)
            assertNull(underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN both fields invalid WHEN onEmailSignIn THEN both fields show errors`() = runTest {
            underTest.onEmailSignIn("bad", "12345")
            advanceUntilIdle()

            assertEquals(AuthError.INVALID_EMAIL, underTest.uiState.value.fieldErrors.emailError)
            assertEquals(AuthError.PASSWORD_TOO_SHORT, underTest.uiState.value.fieldErrors.passwordError)
        }

        @Test
        fun `GIVEN invalid credentials WHEN onEmailSignIn THEN password field shows error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn(DEFAULT_USER, "${DEFAULT_PASS}2")
            advanceUntilIdle()

            assertEquals(AuthError.INVALID_CREDENTIALS, underTest.uiState.value.fieldErrors.passwordError)
            assertNull(underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN user not found WHEN onEmailSignIn THEN email field shows error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn("luke.skywalker@rebellion.glx", DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(AuthError.USER_NOT_FOUND, underTest.uiState.value.fieldErrors.emailError)
            assertNull(underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN unexpected exception WHEN onEmailSignIn THEN state shows general error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)
            fakeAuthService.withError(RuntimeException("Something unexpected"))

            underTest.onEmailSignIn(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(AuthError.SIGN_IN_FAILED, underTest.uiState.value.generalError)
        }
    }

    @Nested
    internal inner class EmailSignUp {
        @Test
        fun `GIVEN valid credentials WHEN onEmailSignUp THEN state transitions to Success`() = runTest {
            underTest.onEmailSignUp(DEFAULT_DISPLAY_NAME, DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertTrue(underTest.uiState.value.isSuccess)
        }

        @Test
        fun `GIVEN empty fields WHEN onEmailSignUp THEN all fields show errors`() = runTest {
            underTest.onEmailSignUp("", "", "")
            advanceUntilIdle()

            assertEquals(AuthError.EMPTY_FIELD, underTest.uiState.value.fieldErrors.displayNameError)
            assertEquals(AuthError.EMPTY_FIELD, underTest.uiState.value.fieldErrors.emailError)
            assertEquals(AuthError.EMPTY_FIELD, underTest.uiState.value.fieldErrors.passwordError)
        }

        @Test
        fun `GIVEN short display name WHEN onEmailSignUp THEN display name field shows error`() = runTest {
            underTest.onEmailSignUp("A", DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(AuthError.DISPLAY_NAME_TOO_SHORT, underTest.uiState.value.fieldErrors.displayNameError)
            assertNull(underTest.uiState.value.fieldErrors.emailError)
        }

        @Test
        fun `GIVEN account collision WHEN onEmailSignUp THEN email field shows error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignUp(DEFAULT_DISPLAY_NAME, DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(AuthError.ACCOUNT_ALREADY_EXISTS, underTest.uiState.value.fieldErrors.emailError)
            assertNull(underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN weak password WHEN onEmailSignUp THEN password field shows error`() = runTest {
            underTest.onEmailSignUp(DEFAULT_DISPLAY_NAME, DEFAULT_USER, "pass")
            advanceUntilIdle()

            assertEquals(AuthError.PASSWORD_TOO_SHORT, underTest.uiState.value.fieldErrors.passwordError)
        }

        @Test
        fun `GIVEN unexpected exception WHEN onEmailSignUp THEN state shows general error`() = runTest {
            fakeAuthService.withError(RuntimeException("Email taken"))

            underTest.onEmailSignUp(DEFAULT_DISPLAY_NAME, DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(AuthError.SIGN_UP_FAILED, underTest.uiState.value.generalError)
        }
    }

    @Nested
    internal inner class ForgotPassword {
        @Test
        fun `GIVEN valid email with existing user WHEN onForgotPassword THEN password reset sent`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onForgotPassword(DEFAULT_USER)
            advanceUntilIdle()

            assertTrue(underTest.uiState.value.isPasswordResetSent)
            assertFalse(underTest.uiState.value.isLoading)
        }

        @Test
        fun `GIVEN empty email WHEN onForgotPassword THEN email field shows error`() = runTest {
            underTest.onForgotPassword("")
            advanceUntilIdle()

            assertEquals(AuthError.EMPTY_FIELD, underTest.uiState.value.fieldErrors.emailError)
            assertFalse(underTest.uiState.value.isPasswordResetSent)
        }

        @Test
        fun `GIVEN invalid email WHEN onForgotPassword THEN email field shows error`() = runTest {
            underTest.onForgotPassword("not-an-email")
            advanceUntilIdle()

            assertEquals(AuthError.INVALID_EMAIL, underTest.uiState.value.fieldErrors.emailError)
            assertFalse(underTest.uiState.value.isPasswordResetSent)
        }

        @Test
        fun `GIVEN user not found WHEN onForgotPassword THEN email field shows specific error`() = runTest {
            underTest.onForgotPassword("unknown@example.com")
            advanceUntilIdle()

            assertEquals(
                AuthError.PASSWORD_RESET_USER_NOT_FOUND,
                underTest.uiState.value.fieldErrors.emailError,
            )
            assertFalse(underTest.uiState.value.isPasswordResetSent)
        }

        @Test
        fun `GIVEN network error WHEN onForgotPassword THEN shows general error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)
            fakeAuthService.disconnect()

            underTest.onForgotPassword(DEFAULT_USER)
            advanceUntilIdle()

            assertEquals(AuthError.NETWORK_ERROR, underTest.uiState.value.generalError)
        }
    }

    @Nested
    internal inner class ClearError {
        @Test
        fun `GIVEN general error WHEN clearError THEN general error is cleared`() = runTest {
            fakeAuthService.withError(RuntimeException("fail"))
            underTest.onEmailSignIn(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            underTest.clearError()

            assertNull(underTest.uiState.value.generalError)
        }

        @Test
        fun `GIVEN field errors WHEN clearFieldErrors THEN field errors are cleared`() = runTest {
            underTest.onEmailSignIn("", "")
            advanceUntilIdle()

            assertTrue(underTest.uiState.value.fieldErrors.hasErrors)

            underTest.clearFieldErrors()

            val state = underTest.uiState.value
            assertNull(state.fieldErrors.emailError)
            assertNull(state.fieldErrors.passwordError)
            assertNull(state.fieldErrors.displayNameError)
        }

        @Test
        fun `GIVEN password reset sent WHEN clearError THEN reset flag is cleared`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)
            underTest.onForgotPassword(DEFAULT_USER)
            advanceUntilIdle()

            assertTrue(underTest.uiState.value.isPasswordResetSent)

            underTest.clearError()

            assertFalse(underTest.uiState.value.isPasswordResetSent)
        }
    }

    companion object {
        private const val GOOGLE_TOKEN = "google-token-42"
        private const val DEFAULT_USER = "darth.vader@empire.glx"
        private const val DEFAULT_PASS = "MyNameIsAnakin"
        private const val DEFAULT_DISPLAY_NAME = "DarthVader"
    }
}
