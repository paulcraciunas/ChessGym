package com.paulcraciunas.chessgym

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.paulcraciunas.chessgym.debug.DebugMenuProvider
import com.paulcraciunas.domain.api.achievements.FakeAchievementNotificationManager
import com.paulcraciunas.domain.api.auth.DeleteAccountUseCase
import com.paulcraciunas.domain.api.auth.SignOutUseCase
import com.paulcraciunas.domain.impl.auth.DeleteAccountUseCaseImpl
import com.paulcraciunas.domain.impl.auth.SignOutUseCaseImpl
import com.paulcraciunas.global.device.api.fakes.FakeGetNetworkState
import com.paulcraciunas.global.device.api.usecases.GetNetworkState.NetworkState
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import com.paulcraciunas.user.api.FakeAuthService
import com.paulcraciunas.user.api.FakeSyncScheduler
import com.paulcraciunas.user.api.FakeSyncState
import com.paulcraciunas.user.api.FakeTokenProvider
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
internal class MainScreenViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeAppSettingsRepository = FakeAppSettingsRepository()
    private val fakeUserRepository = FakeUserRepository()
    private val fakeTokenProvider = FakeTokenProvider()
    private val fakeSyncState = FakeSyncState()
    private val fakeSyncScheduler = FakeSyncScheduler()
    private val fakeAuthService = FakeAuthService()
    private val fakeGetNetworkState = FakeGetNetworkState()

    private lateinit var underTest: MainScreenViewModel

    private val defaultSignOutUseCase = SignOutUseCaseImpl(
        userRepository = fakeUserRepository,
        tokenProvider = fakeTokenProvider,
        syncState = fakeSyncState,
        syncScheduler = fakeSyncScheduler,
    )
    private val defaultDeleteAccountUseCase = DeleteAccountUseCaseImpl(
        userRepository = fakeUserRepository,
        authService = fakeAuthService,
        tokenProvider = fakeTokenProvider,
        syncState = fakeSyncState,
        syncScheduler = fakeSyncScheduler,
        getNetworkState = fakeGetNetworkState,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = createViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class UiState {
        @Test
        fun `GIVEN initial state WHEN observed THEN isLoading is true`() = runTest {
            assertTrue(underTest.uiState.value.isLoading)
        }

        @Test
        fun `GIVEN app settings emitted WHEN observed THEN isLoading becomes false`() = runTest {
            observeUiState()
            fakeAppSettingsRepository.setAppSettings(FakeAppSettingsRepository.defaultSettings())
            advanceUntilIdle()

            assertFalse(underTest.uiState.value.isLoading)
        }

        @Test
        fun `GIVEN no authentication WHEN observed THEN isSignedIn is false`() = runTest {
            observeUiState()
            advanceUntilIdle()

            assertFalse(underTest.uiState.value.isSignedIn)
        }

        @Test
        fun `GIVEN signed-in user WHEN observed THEN isSignedIn is true`() = runTest {
            observeUiState()
            signInUser()
            advanceUntilIdle()

            assertTrue(underTest.uiState.value.isSignedIn)
        }

        @Test
        fun `GIVEN initial state WHEN observed THEN activeDialog is null`() = runTest {
            observeUiState()
            advanceUntilIdle()

            assertNull(underTest.uiState.value.activeDialog)
        }
    }

    @Nested
    internal inner class DialogState {
        @Test
        fun `WHEN showSignOutDialog THEN activeDialog is SignOutConfirmation`() = runTest {
            observeUiState()
            advanceUntilIdle()

            underTest.showSignOutDialog()
            advanceUntilIdle()

            assertEquals(
                MainScreenDialog.SignOutConfirmation,
                underTest.uiState.value.activeDialog,
            )
        }

        @Test
        fun `WHEN showDeleteAccountDialog THEN activeDialog is DeleteAccountConfirmation`() = runTest {
            observeUiState()
            advanceUntilIdle()

            underTest.showDeleteAccountDialog()
            advanceUntilIdle()

            assertEquals(
                MainScreenDialog.DeleteAccountConfirmation,
                underTest.uiState.value.activeDialog,
            )
        }

        @Test
        fun `GIVEN active dialog WHEN dismissDialog THEN activeDialog is null`() = runTest {
            observeUiState()
            advanceUntilIdle()

            underTest.showSignOutDialog()
            advanceUntilIdle()
            underTest.dismissDialog()
            advanceUntilIdle()

            assertNull(underTest.uiState.value.activeDialog)
        }
    }

    @Nested
    internal inner class SignOut {
        @Test
        fun `GIVEN signed-in user WHEN signOut THEN isSignedIn becomes false`() = runTest {
            observeUiState()
            signInUser()
            advanceUntilIdle()
            assertTrue(underTest.uiState.value.isSignedIn)

            underTest.signOut()
            advanceUntilIdle()

            assertFalse(underTest.uiState.value.isSignedIn)
        }

        @Test
        fun `GIVEN signed-in user WHEN signOut THEN emits SignedOut event`() = runTest {
            signInUser()
            advanceUntilIdle()

            val events = collectAccountEvents()

            underTest.signOut()
            advanceUntilIdle()

            assertEquals(listOf(AccountEvent.SignedOut), events)
        }

        @Test
        fun `GIVEN sign-out fails WHEN signOut THEN emits SignOutFailed event`() = runTest {
            val failingSignOut = object : SignOutUseCase {
                override suspend fun invoke() = throw RuntimeException("Sign out failed")
            }
            val failingViewModel = createViewModel(signOutUseCase = failingSignOut)
            signInUser()
            advanceUntilIdle()

            val events = mutableListOf<AccountEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
                failingViewModel.accountEvent.collect { events.add(it) }
            }

            failingViewModel.signOut()
            advanceUntilIdle()

            assertEquals(listOf(AccountEvent.SignOutFailed), events)
        }
    }

    @Nested
    internal inner class DeleteAccount {
        @Test
        fun `GIVEN signed-in user WHEN deleteAccount THEN isSignedIn becomes false`() = runTest {
            observeUiState()
            signInUser()
            advanceUntilIdle()
            assertTrue(underTest.uiState.value.isSignedIn)

            underTest.deleteAccount()
            advanceUntilIdle()

            assertFalse(underTest.uiState.value.isSignedIn)
        }

        @Test
        fun `GIVEN signed-in user WHEN deleteAccount THEN emits AccountDeleted event`() = runTest {
            signInUser()
            advanceUntilIdle()

            val events = collectAccountEvents()

            underTest.deleteAccount()
            advanceUntilIdle()

            assertEquals(listOf(AccountEvent.AccountDeleted), events)
        }

        @Test
        fun `GIVEN no network WHEN deleteAccount THEN emits NoNetwork failure event`() = runTest {
            signInUser()
            fakeGetNetworkState.setState(NetworkState.Disconnected)
            advanceUntilIdle()

            val events = collectAccountEvents()

            underTest.deleteAccount()
            advanceUntilIdle()

            assertEquals(
                listOf(AccountEvent.DeleteAccountFailed(AccountEvent.DeleteAccountFailReason.NO_NETWORK)),
                events,
            )
        }

        @Test
        fun `GIVEN auth service error WHEN deleteAccount THEN emits Unknown failure event`() = runTest {
            signInUser()
            fakeAuthService.withError(RuntimeException("Server error"))
            advanceUntilIdle()

            val events = collectAccountEvents()

            underTest.deleteAccount()
            advanceUntilIdle()

            assertEquals(
                listOf(AccountEvent.DeleteAccountFailed(AccountEvent.DeleteAccountFailReason.UNKNOWN)),
                events,
            )
        }
    }

    private fun createViewModel(
        signOutUseCase: SignOutUseCase = defaultSignOutUseCase,
        deleteAccountUseCase: DeleteAccountUseCase = defaultDeleteAccountUseCase,
    ): MainScreenViewModel = MainScreenViewModel(
        appSettingsRepository = fakeAppSettingsRepository,
        userRepository = fakeUserRepository,
        signOutUseCase = signOutUseCase,
        deleteAccountUseCase = deleteAccountUseCase,
        debugMenuProvider = NoOpDebugMenuProvider(),
        achievementNotificationManager = FakeAchievementNotificationManager(),
    )

    private fun TestScope.observeUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            underTest.uiState.collect {}
        }
    }

    private fun TestScope.collectAccountEvents(): MutableList<AccountEvent> {
        val events = mutableListOf<AccountEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            underTest.accountEvent.collect { events.add(it) }
        }
        return events
    }

    private suspend fun signInUser() {
        val authState = User.AuthenticationState(
            userId = UserDefaults.USER_ID,
            provider = User.AuthenticationState.AuthProvider.GOOGLE,
        )
        fakeUserRepository.signIn(authState)
    }

    private class NoOpDebugMenuProvider : DebugMenuProvider {
        @Composable
        override fun ColumnScope.DrawerContent(
            closeDrawer: () -> Unit,
            onNavigate: (Any) -> Unit,
        ) {}

        override fun NavGraphBuilder.registerDebugScreens(navController: NavHostController) {}
    }
}
