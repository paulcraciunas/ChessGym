package com.paulcraciunas.screens.signin.vm

sealed class SignInUiState {
    data object Idle : SignInUiState()
    data object Loading : SignInUiState()
    data object Success : SignInUiState()
    data class Error(val error: AuthError) : SignInUiState()
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
    EMPTY_FIELDS,
    INVALID_EMAIL,
    PASSWORD_TOO_SHORT,
    SESSION_EXPIRED,
    ACCOUNT_DISABLED,
    SERVER_ERROR,
}
