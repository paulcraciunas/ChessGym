package com.paulcraciunas.chessgym.main

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.auth.DeleteAccountResult
import com.paulcraciunas.domain.api.auth.DeleteAccountUseCase
import com.paulcraciunas.domain.api.auth.SignOutUseCase
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
    val isSignedIn: Boolean = false,
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
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    userRepository: UserRepository,
) : ViewModel() {
    private val _dialogState = MutableStateFlow<MainScreenDialog?>(null)
    private val _accountEvent = Channel<AccountEvent>(Channel.BUFFERED)

    val uiState: StateFlow<MainScreenUiState> = combine(
        userRepository.userUpdates().map { it.isSignedIn() },
        _dialogState
    ) { isSignedIn, activeDialog ->
        MainScreenUiState(
            isSignedIn = isSignedIn,
            activeDialog = activeDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MainScreenUiState()
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
