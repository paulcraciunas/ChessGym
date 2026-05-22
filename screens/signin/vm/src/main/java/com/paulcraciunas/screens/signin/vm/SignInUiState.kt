package com.paulcraciunas.screens.signin.vm

data class SignInUiState(
    val screenState: ScreenState = ScreenState.IDLE,
    val fieldErrors: FieldErrors = FieldErrors(),
    val generalError: AuthError? = null,
    val isPasswordResetSent: Boolean = false,
) {
    val isLoading: Boolean = screenState == ScreenState.LOADING
    val isSuccess: Boolean = screenState == ScreenState.SUCCESS
}

enum class ScreenState {
    IDLE,
    LOADING,
    SUCCESS,
}

data class FieldErrors(
    val displayNameError: AuthError? = null,
    val emailError: AuthError? = null,
    val passwordError: AuthError? = null,
) {
    val hasErrors: Boolean
        get() = displayNameError != null || emailError != null || passwordError != null
}

enum class AuthError {
    GOOGLE_SIGN_IN_FAILED,
    SIGN_IN_FAILED,
    SIGN_UP_FAILED,
    INVALID_CREDENTIALS,
    USER_NOT_FOUND,
    ACCOUNT_ALREADY_EXISTS,
    WEAK_PASSWORD,
    NETWORK_ERROR,
    EMPTY_FIELD,
    INVALID_EMAIL,
    PASSWORD_TOO_SHORT,
    DISPLAY_NAME_TOO_SHORT,
    DISPLAY_NAME_TOO_LONG,
    NO_GOOGLE_ACCOUNTS,
    SESSION_EXPIRED,
    ACCOUNT_DISABLED,
    SERVER_ERROR,
    PASSWORD_RESET_USER_NOT_FOUND,
}
