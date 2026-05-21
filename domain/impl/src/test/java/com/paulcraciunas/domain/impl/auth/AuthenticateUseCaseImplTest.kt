package com.paulcraciunas.domain.impl.auth

import com.paulcraciunas.domain.api.auth.AuthenticateUseCase.Credentials
import com.paulcraciunas.user.api.AuthException
import com.paulcraciunas.user.api.FakeAuthService
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AuthenticateUseCaseImplTest {
    private val fakeAuthService = FakeAuthService()
    private val fakeUserRepository = FakeUserRepository()
    private val underTest = AuthenticateUseCaseImpl(fakeAuthService, fakeUserRepository)

    @Nested
    internal inner class GoogleCredentials {
        @Test
        fun `GIVEN valid token WHEN invoke with Google THEN authenticates and returns signed-in user`() = runTest {
            fakeAuthService.withTokenUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            val result = underTest(Credentials.Google { GOOGLE_TOKEN })

            assertEquals(UserDefaults.USER_ID, result.authentication?.userId)
            assertEquals(
                User.AuthenticationState.AuthProvider.GOOGLE,
                result.authentication?.provider,
            )
        }

        @Test
        fun `GIVEN auth service throws WHEN invoke with Google THEN exception propagates`() = runTest {
            fakeAuthService.withTokenUser(GOOGLE_TOKEN, UserDefaults.USER_ID)
            fakeAuthService.disconnect()

            assertThrows<AuthException.NetworkError> {
                underTest(Credentials.Google { GOOGLE_TOKEN })
            }
        }

        @Test
        fun `GIVEN auth service user is unknown WHEN invoke with Google THEN exception propagates`() = runTest {
            // Don't register the user
            // fakeAuthService.withExistingUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            assertThrows<AuthException.UserNotFound> {
                underTest(Credentials.Google { GOOGLE_TOKEN })
            }
        }
    }

    @Nested
    internal inner class EmailCredentials {
        @Test
        fun `GIVEN valid credentials WHEN invoke with Email THEN authenticates and returns signed-in user`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_EMAIL, DEFAULT_PASS, UserDefaults.USER_ID)

            val result = underTest(Credentials.Email(DEFAULT_EMAIL, DEFAULT_PASS))

            assertEquals(UserDefaults.USER_ID, result.authentication?.userId)
            assertEquals(
                User.AuthenticationState.AuthProvider.EMAIL,
                result.authentication?.provider,
            )
        }

        @Test
        fun `GIVEN invalid credentials WHEN invoke with Email THEN exception propagates`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_EMAIL, DEFAULT_PASS, UserDefaults.USER_ID)

            assertThrows<AuthException.InvalidCredentials> {
                underTest(Credentials.Email(DEFAULT_EMAIL, "wrong"))
            }
        }

        @Test
        fun `GIVEN invalid user WHEN invoke with Email THEN exception propagates`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_EMAIL, DEFAULT_PASS, UserDefaults.USER_ID)

            assertThrows<AuthException.UserNotFound> {
                underTest(Credentials.Email("lukeSkywalker@rebellion.com", DEFAULT_PASS))
            }
        }
    }

    @Nested
    internal inner class NewAccountCredentials {
        @Test
        fun `GIVEN valid credentials WHEN invoke with NewAccount THEN creates account and returns signed-in user`() = runTest {
            val result = underTest(Credentials.NewAccount(DEFAULT_EMAIL, DEFAULT_PASS, DEFAULT_DISPLAY_NAME))

            assertEquals(UserDefaults.USER_ID, result.authentication?.userId)
            assertEquals(
                User.AuthenticationState.AuthProvider.EMAIL,
                result.authentication?.provider,
            )
        }

        @Test
        fun `GIVEN valid credentials WHEN invoke with NewAccount THEN display name is saved`() = runTest {
            val result = underTest(Credentials.NewAccount(DEFAULT_EMAIL, DEFAULT_PASS, DEFAULT_DISPLAY_NAME))

            assertEquals(DEFAULT_DISPLAY_NAME, result.profile.displayName)
            val storedUser = fakeUserRepository.get()
            assertEquals(DEFAULT_DISPLAY_NAME, storedUser.profile.displayName)
        }

        @Test
        fun `GIVEN account exists WHEN invoke with NewAccount THEN exception propagates`() = runTest {
            fakeAuthService.withExistingUser(DEFAULT_EMAIL, DEFAULT_PASS, UserDefaults.USER_ID)

            assertThrows<AuthException.AccountCollision> {
                underTest(Credentials.NewAccount(DEFAULT_EMAIL, DEFAULT_PASS, DEFAULT_DISPLAY_NAME))
            }
        }

        @Test
        fun `GIVEN password is too short WHEN invoke with NewAccount THEN exception propagates`() = runTest {
            assertThrows<AuthException.WeakPassword> {
                underTest(Credentials.NewAccount(DEFAULT_EMAIL, "pass", DEFAULT_DISPLAY_NAME))
            }
        }
    }

    @Nested
    internal inner class RepositoryIntegration {
        @Test
        fun `GIVEN successful auth WHEN invoke THEN user is persisted via repository signIn`() = runTest {
            fakeAuthService.withTokenUser(GOOGLE_TOKEN, UserDefaults.USER_ID)

            val result = underTest(Credentials.Google { GOOGLE_TOKEN })

            assertEquals(UserDefaults.USER_ID, result.authentication?.userId)
            val storedUser = fakeUserRepository.get()
            assertEquals(UserDefaults.USER_ID, storedUser.authentication?.userId)
        }
    }

    companion object {
        private const val GOOGLE_TOKEN = "google-token-123"
        private const val DEFAULT_EMAIL = "darth.vader@theempire.glx"
        private const val DEFAULT_PASS = "myRealNameIsAnakin"
        private const val DEFAULT_DISPLAY_NAME = "DarthVader"
    }
}
