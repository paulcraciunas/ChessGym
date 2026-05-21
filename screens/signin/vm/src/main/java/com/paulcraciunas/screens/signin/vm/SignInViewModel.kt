package com.paulcraciunas.screens.signin.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.auth.AuthenticateUseCase
import com.paulcraciunas.domain.api.auth.AuthenticateUseCase.Credentials
import com.paulcraciunas.domain.api.auth.ResetPasswordUseCase
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
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onGoogleSignIn(tokenSource: TokenSource) {
        authenticate(Credentials.Google(tokenSource), AuthError.GOOGLE_SIGN_IN_FAILED)
    }

    fun onEmailSignIn(email: String, password: String) {
        val fieldErrors = validateSignInFields(email = email, password = password)
        if (fieldErrors.hasErrors) {
            _uiState.update { it.copy(fieldErrors = fieldErrors, generalError = null) }
            return
        }
        authenticate(Credentials.Email(email, password), AuthError.SIGN_IN_FAILED)
    }

    fun onEmailSignUp(displayName: String, email: String, password: String) {
        val fieldErrors = validateSignUpFields(
            displayName = displayName,
            email = email,
            password = password,
        )
        if (fieldErrors.hasErrors) {
            _uiState.update { it.copy(fieldErrors = fieldErrors, generalError = null) }
            return
        }
        authenticate(Credentials.NewAccount(email, password, displayName), AuthError.SIGN_UP_FAILED)
    }

    fun onForgotPassword(email: String) {
        val emailError = validateEmail(email)
        if (emailError != null) {
            _uiState.update {
                it.copy(fieldErrors = FieldErrors(emailError = emailError), generalError = null)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    screenState = ScreenState.LOADING,
                    fieldErrors = FieldErrors(),
                    generalError = null,
                    isPasswordResetSent = false,
                )
            }
            try {
                resetPasswordUseCase(email)
                _uiState.update {
                    it.copy(screenState = ScreenState.IDLE, isPasswordResetSent = true)
                }
            } catch (e: AuthException.UserNotFound) {
                Timber.w(e, "Password reset failed: no user")
                _uiState.update {
                    it.copy(
                        screenState = ScreenState.IDLE,
                        fieldErrors = FieldErrors(
                            emailError = AuthError.PASSWORD_RESET_USER_NOT_FOUND,
                        ),
                    )
                }
            } catch (e: AuthException) {
                Timber.w(e, "Password reset failed")
                _uiState.update {
                    it.copy(
                        screenState = ScreenState.IDLE,
                        generalError = e.toAuthError(),
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(generalError = null, isPasswordResetSent = false) }
    }

    fun clearFieldErrors() {
        _uiState.update { it.copy(fieldErrors = FieldErrors()) }
    }

    private fun authenticate(credentials: Credentials, fallbackError: AuthError) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    screenState = ScreenState.LOADING,
                    fieldErrors = FieldErrors(),
                    generalError = null,
                )
            }
            try {
                authenticateUseCase(credentials)
                _uiState.update { it.copy(screenState = ScreenState.SUCCESS) }
            } catch (e: AuthException) {
                Timber.w(e, "Authentication failed")
                handleAuthError(e.toAuthError())
            } catch (e: UserApiException) {
                Timber.w(e, "User API failure")
                handleAuthError(e.toAuthError())
            } catch (e: Exception) {
                Timber.w(e, "Unexpected authentication failure")
                handleAuthError(fallbackError)
            }
        }
    }

    private fun handleAuthError(error: AuthError) {
        val fieldErrors = error.toFieldErrors()
        _uiState.update {
            it.copy(
                screenState = ScreenState.IDLE,
                fieldErrors = fieldErrors,
                generalError = if (fieldErrors.hasErrors) null else error,
            )
        }
    }

    private fun validateSignInFields(email: String, password: String): FieldErrors =
        FieldErrors(
            emailError = validateEmail(email),
            passwordError = validatePassword(password),
        )

    private fun validateSignUpFields(
        displayName: String,
        email: String,
        password: String,
    ): FieldErrors = FieldErrors(
        displayNameError = validateDisplayName(displayName),
        emailError = validateEmail(email),
        passwordError = validatePassword(password),
    )

    private fun validateEmail(email: String): AuthError? = when {
        email.isBlank() -> AuthError.EMPTY_FIELD
        !emailValidator.matches(email) -> AuthError.INVALID_EMAIL
        else -> null
    }

    private fun validatePassword(password: String): AuthError? = when {
        password.isBlank() -> AuthError.EMPTY_FIELD
        password.length <= MIN_PASSWORD_LENGTH -> AuthError.PASSWORD_TOO_SHORT
        else -> null
    }

    private fun validateDisplayName(displayName: String): AuthError? = when {
        displayName.isBlank() -> AuthError.EMPTY_FIELD
        displayName.length < MIN_DISPLAY_NAME_LENGTH -> AuthError.DISPLAY_NAME_TOO_SHORT
        else -> null
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MIN_DISPLAY_NAME_LENGTH = 4
        private val emailValidator = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
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

private fun AuthError.toFieldErrors(): FieldErrors = when (this) {
    AuthError.INVALID_CREDENTIALS -> FieldErrors(passwordError = this)
    AuthError.USER_NOT_FOUND -> FieldErrors(emailError = this)
    AuthError.ACCOUNT_ALREADY_EXISTS -> FieldErrors(emailError = this)
    AuthError.WEAK_PASSWORD -> FieldErrors(passwordError = this)
    AuthError.INVALID_EMAIL -> FieldErrors(emailError = this)
    else -> FieldErrors()
}
