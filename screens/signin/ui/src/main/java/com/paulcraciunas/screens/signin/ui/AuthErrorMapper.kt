package com.paulcraciunas.screens.signin.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paulcraciunas.screens.signin.vm.AuthError
import com.paulcraciunas.global.resources.R as GlobalR

@Composable
internal fun AuthError?.toMessage(): String = when (this) {
    AuthError.GOOGLE_SIGN_IN_FAILED -> stringResource(GlobalR.string.auth_error_google_sign_in_failed)
    AuthError.SIGN_IN_FAILED -> stringResource(GlobalR.string.auth_error_sign_in_failed)
    AuthError.SIGN_UP_FAILED -> stringResource(GlobalR.string.auth_error_sign_up_failed)
    AuthError.INVALID_CREDENTIALS -> stringResource(GlobalR.string.auth_error_invalid_credentials)
    AuthError.USER_NOT_FOUND -> stringResource(GlobalR.string.auth_error_user_not_found)
    AuthError.ACCOUNT_ALREADY_EXISTS -> stringResource(GlobalR.string.auth_error_account_already_exists)
    AuthError.WEAK_PASSWORD -> stringResource(GlobalR.string.auth_error_weak_password)
    AuthError.NETWORK_ERROR -> stringResource(GlobalR.string.auth_error_network)
    AuthError.EMPTY_FIELDS -> stringResource(GlobalR.string.auth_error_empty_fields)
    AuthError.INVALID_EMAIL -> stringResource(GlobalR.string.auth_error_invalid_email)
    AuthError.PASSWORD_TOO_SHORT -> stringResource(GlobalR.string.auth_error_password_too_short)
    AuthError.SESSION_EXPIRED -> stringResource(GlobalR.string.auth_error_session_expired)
    AuthError.ACCOUNT_DISABLED -> stringResource(GlobalR.string.auth_error_account_disabled)
    AuthError.SERVER_ERROR -> stringResource(GlobalR.string.auth_error_server)
    null -> ""
}
