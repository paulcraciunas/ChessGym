package com.paulcraciunas.screens.signin.vm

import com.paulcraciunas.domain.impl.auth.AuthenticateUseCaseImpl
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
            )
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
            fakeAuthService.withExistingUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            underTest.onGoogleSignIn { GOOGLE_TOKEN }
            advanceUntilIdle()

            assertEquals(SignInUiState.Success, underTest.uiState.value)
        }

        @Test
        fun `GIVEN unexpected exception WHEN onGoogleSignIn THEN state shows fallback error`() = runTest {
            fakeAuthService.withError(IllegalStateException("Unexpected"))
            fakeAuthService.withExistingUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            underTest.onGoogleSignIn { GOOGLE_TOKEN }
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.GOOGLE_SIGN_IN_FAILED),
                underTest.uiState.value,
            )
        }

        @Test
        fun `GIVEN no credentials WHEN onGoogleSignIn THEN state shows no google accounts error`() = runTest {
            underTest.onGoogleSignIn { throw AuthException.NoCredentials() }
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.NO_GOOGLE_ACCOUNTS),
                underTest.uiState.value,
            )
        }

        @Test
        fun `GIVEN network error WHEN onGoogleSignIn THEN state shows network error`() = runTest {
            fakeAuthService.disconnect()
            fakeAuthService.withExistingUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            underTest.onGoogleSignIn { GOOGLE_TOKEN }
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.NETWORK_ERROR),
                underTest.uiState.value,
            )
        }
    }

    @Nested
    internal inner class EmailSignIn {
        @Test
        fun `GIVEN valid credentials WHEN onEmailSignIn THEN state transitions to Success`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(SignInUiState.Success, underTest.uiState.value)
        }

        @Test
        fun `GIVEN empty email WHEN onEmailSignIn THEN state transitions to Error without calling use case`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn("", DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(SignInUiState.Error(AuthError.EMPTY_FIELDS), underTest.uiState.value)
        }

        @Test
        fun `GIVEN invalid email format WHEN onEmailSignIn THEN state transitions to INVALID_EMAIL`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn("not-an-email", DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(SignInUiState.Error(AuthError.INVALID_EMAIL), underTest.uiState.value)
        }

        @Test
        fun `GIVEN short password WHEN onEmailSignIn THEN state transitions to Error without calling use case`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn(DEFAULT_USER, "12345")
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.PASSWORD_TOO_SHORT),
                underTest.uiState.value,
            )
        }

        @Test
        fun `GIVEN invalid credentials WHEN onEmailSignIn THEN state shows specific error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn(DEFAULT_USER, "${DEFAULT_PASS}2")
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.INVALID_CREDENTIALS),
                underTest.uiState.value,
            )
        }

        @Test
        fun `GIVEN user not found WHEN onEmailSignIn THEN state shows specific error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignIn("luke.skywalker@rebellion.glx", DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.USER_NOT_FOUND),
                underTest.uiState.value,
            )
        }

        @Test
        fun `GIVEN unexpected exception WHEN onEmailSignIn THEN state shows fallback error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)
            fakeAuthService.withError(RuntimeException("Something unexpected"))

            underTest.onEmailSignIn(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(SignInUiState.Error(AuthError.SIGN_IN_FAILED), underTest.uiState.value)
        }
    }

    @Nested
    internal inner class EmailSignUp {
        @Test
        fun `GIVEN valid credentials WHEN onEmailSignUp THEN state transitions to Success`() = runTest {
            underTest.onEmailSignUp(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(SignInUiState.Success, underTest.uiState.value)
        }

        @Test
        fun `GIVEN empty fields WHEN onEmailSignUp THEN state transitions to Error`() = runTest {
            underTest.onEmailSignUp("", "")
            advanceUntilIdle()

            assertEquals(SignInUiState.Error(AuthError.EMPTY_FIELDS), underTest.uiState.value)
        }

        @Test
        fun `GIVEN account collision WHEN onEmailSignUp THEN state shows specific error`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_USER, DEFAULT_PASS, UserDefaults.USER_ID)

            underTest.onEmailSignUp(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.ACCOUNT_ALREADY_EXISTS),
                underTest.uiState.value,
            )
        }

        @Test
        fun `GIVEN weak password WHEN onEmailSignUp THEN state shows specific error`() = runTest {
            underTest.onEmailSignUp(DEFAULT_USER, "pass")
            advanceUntilIdle()

            assertEquals(
                SignInUiState.Error(AuthError.PASSWORD_TOO_SHORT),
                underTest.uiState.value,
            )
        }

        @Test
        fun `GIVEN unexpected exception WHEN onEmailSignUp THEN state shows fallback error`() = runTest {
            fakeAuthService.withError(RuntimeException("Email taken"))

            underTest.onEmailSignUp(DEFAULT_USER, DEFAULT_PASS)
            advanceUntilIdle()

            assertEquals(SignInUiState.Error(AuthError.SIGN_UP_FAILED), underTest.uiState.value)
        }
    }

    @Nested
    internal inner class ClearError {
        @Test
        fun `GIVEN error state WHEN clearError THEN state returns to Idle`() = runTest {
            underTest.onEmailSignIn("", "")
            advanceUntilIdle()

            assertTrue(underTest.uiState.value is SignInUiState.Error)

            underTest.clearError()

            assertEquals(SignInUiState.Idle, underTest.uiState.value)
        }
    }

    companion object {
        private const val GOOGLE_TOKEN = "google-token-42"
        private const val DEFAULT_USER = "darth.vader@empire.glx"
        private const val DEFAULT_PASS = "MyNameIsAnakin"
    }
}
