package com.paulcraciunas.chessgym.main

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.chessgym.debug.DebugMenuProvider
import com.paulcraciunas.domain.api.achievements.AchievementNotificationManager
import com.paulcraciunas.domain.api.auth.DeleteAccountResult
import com.paulcraciunas.domain.api.auth.DeleteAccountUseCase
import com.paulcraciunas.domain.api.auth.SignOutUseCase
import com.paulcraciunas.screens.common.UiSettings
import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Immutable
data class MainScreenUiState(
    val appSettings: UiSettings = UiSettings.default(),
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = true,
    val activeDialog: MainScreenDialog? = null,
)

@Immutable
sealed class AccountEvent {
    data object SignedOut : AccountEvent()
    data object SignOutFailed : AccountEvent()
    data object AccountDeleted : AccountEvent()
    data class DeleteAccountFailed(val reason: DeleteAccountFailReason) : AccountEvent()

    enum class DeleteAccountFailReason { NO_NETWORK, UNKNOWN }
}

@Immutable
sealed interface MainScreenDialog {
    data object SignOutConfirmation : MainScreenDialog
    data object DeleteAccountConfirmation : MainScreenDialog
}

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    appSettingsRepository: AppSettingsRepository,
    userRepository: UserRepository,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    val debugMenuProvider: DebugMenuProvider,
    val achievementNotificationManager: AchievementNotificationManager,
) : ViewModel() {
    private val _dialogState = MutableStateFlow<MainScreenDialog?>(null)
    private val _accountEvent = Channel<AccountEvent>(Channel.BUFFERED)

    val uiState: StateFlow<MainScreenUiState> = combine(
        appSettingsRepository.appSettings,
        userRepository.userUpdates().map { it.isSignedIn() },
        _dialogState
    ) { appSettings, isSignedIn, activeDialog ->
        MainScreenUiState(
            appSettings = appSettings.uiSettings(),
            isSignedIn = isSignedIn,
            isLoading = false,
            activeDialog = activeDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MainScreenUiState(isLoading = true)
    )
    val accountEvent = _accountEvent.receiveAsFlow()

    fun signOut() {
        viewModelScope.launch {
            try {
                signOutUseCase()
                _accountEvent.send(AccountEvent.SignedOut)
            } catch (e: Exception) {
                Timber.w(e, "Sign out failed")
                _accountEvent.send(AccountEvent.SignOutFailed)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val event = when (val result = deleteAccountUseCase()) {
                is DeleteAccountResult.Success -> AccountEvent.AccountDeleted
                is DeleteAccountResult.NoNetwork -> AccountEvent.DeleteAccountFailed(AccountEvent.DeleteAccountFailReason.NO_NETWORK)
                is DeleteAccountResult.Failure -> {
                    Timber.w(result.cause, "Delete account failed")
                    AccountEvent.DeleteAccountFailed(AccountEvent.DeleteAccountFailReason.UNKNOWN)
                }
            }
            _accountEvent.send(event)
        }
    }

    fun showSignOutDialog() {
        _dialogState.value = MainScreenDialog.SignOutConfirmation
    }

    fun showDeleteAccountDialog() {
        _dialogState.value = MainScreenDialog.DeleteAccountConfirmation
    }

    fun dismissDialog() {
        _dialogState.value = null
    }
}

private fun AppSettings.uiSettings(): UiSettings = UiSettings(
    lightMode = when (this.lightMode) {
        AppSettings.LightMode.System -> UiSettings.Mode.System
        AppSettings.LightMode.Light -> UiSettings.Mode.Light
        AppSettings.LightMode.Dark -> UiSettings.Mode.Dark
    },
    autoPromote = this.autoPromote,
    autoNextPuzzle = this.autoNextPuzzle,
    showBorders = this.showBorders,
    enableVibrations = this.enableVibrations,
    highlightLegalMoves = this.highlightLegalMoves,
    enableAnimations = this.enableAnimations,
)
