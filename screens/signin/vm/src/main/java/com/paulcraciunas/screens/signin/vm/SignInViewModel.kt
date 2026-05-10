package com.paulcraciunas.screens.signin.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.auth.AuthenticateUseCase
import com.paulcraciunas.domain.api.auth.AuthenticateUseCase.Credentials
import com.paulcraciunas.domain.api.auth.TokenSource
import com.paulcraciunas.user.api.AuthException
import com.paulcraciunas.user.api.UserApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authenticateUseCase: AuthenticateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onGoogleSignIn(tokenSource: TokenSource) {
        authenticate(Credentials.Google(tokenSource), AuthError.GOOGLE_SIGN_IN_FAILED)
    }

    fun onEmailSignIn(email: String, password: String) {
        if (!validate(email = email, password = password)) return
        authenticate(Credentials.Email(email, password), AuthError.SIGN_IN_FAILED)
    }

    fun onEmailSignUp(email: String, password: String) {
        if (!validate(email = email, password = password)) return
        authenticate(Credentials.NewAccount(email, password), AuthError.SIGN_UP_FAILED)
    }

    fun clearError() {
        _uiState.update { SignInUiState.Idle }
    }

    private fun authenticate(credentials: Credentials, fallbackError: AuthError) {
        viewModelScope.launch {
            _uiState.update { SignInUiState.Loading }
            try {
                authenticateUseCase(credentials)
                _uiState.update { SignInUiState.Success }
            } catch (e: AuthException) {
                Timber.w(e, "Authentication failed")
                _uiState.update { SignInUiState.Error(e.toAuthError()) }
            } catch (e: UserApiException) {
                Timber.w(e, "User API failure")
                _uiState.update { SignInUiState.Error(e.toAuthError()) }
            } catch (e: Exception) {
                Timber.w(e, "Unexpected authentication failure")
                _uiState.update { SignInUiState.Error(fallbackError) }
            }
        }
    }

    private fun validate(email: String, password: String): Boolean {
        val emailError = when {
            email.isBlank() -> AuthError.EMPTY_FIELDS
            !EMAIL_PATTERN.matches(email) -> AuthError.INVALID_EMAIL
            else -> null
        }

        val passwordError = when {
            password.isBlank() -> AuthError.EMPTY_FIELDS
            password.length <= MIN_PASSWORD_LENGTH -> AuthError.PASSWORD_TOO_SHORT
            else -> null
        }

        val error = emailError ?: passwordError
        if (error != null) {
            _uiState.update { SignInUiState.Error(error) }
        }
        return error == null
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private val EMAIL_PATTERN = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    }
}

private fun AuthException.toAuthError(): AuthError = when (this) {
    is AuthException.InvalidCredentials -> AuthError.INVALID_CREDENTIALS
    is AuthException.UserNotFound -> AuthError.USER_NOT_FOUND
    is AuthException.AccountCollision -> AuthError.ACCOUNT_ALREADY_EXISTS
    is AuthException.WeakPassword -> AuthError.WEAK_PASSWORD
    is AuthException.NetworkError -> AuthError.NETWORK_ERROR
    is AuthException.Unknown -> AuthError.SIGN_IN_FAILED
    is AuthException.NoCredentials -> AuthError.NO_GOOGLE_ACCOUNTS
}

private fun UserApiException.toAuthError(): AuthError = when (this.statusCode) {
    401 -> AuthError.SESSION_EXPIRED
    403 -> AuthError.ACCOUNT_DISABLED
    in 500..599 -> AuthError.SERVER_ERROR
    else -> AuthError.SIGN_IN_FAILED
}
