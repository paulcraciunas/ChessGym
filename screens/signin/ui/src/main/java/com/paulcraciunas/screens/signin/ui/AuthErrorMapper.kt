package com.paulcraciunas.screens.signin.ui

import android.content.Context
import com.paulcraciunas.screens.signin.vm.AuthError
import com.paulcraciunas.global.resources.R as GlobalR

internal fun AuthError.toMessage(context: Context): String =
    context.getString(
        when (this) {
            AuthError.GOOGLE_SIGN_IN_FAILED -> GlobalR.string.auth_error_google_sign_in_failed
            AuthError.SIGN_IN_FAILED -> GlobalR.string.auth_error_sign_in_failed
            AuthError.SIGN_UP_FAILED -> GlobalR.string.auth_error_sign_up_failed
            AuthError.INVALID_CREDENTIALS -> GlobalR.string.auth_error_invalid_credentials
            AuthError.USER_NOT_FOUND -> GlobalR.string.auth_error_user_not_found
            AuthError.ACCOUNT_ALREADY_EXISTS -> GlobalR.string.auth_error_account_already_exists
            AuthError.WEAK_PASSWORD -> GlobalR.string.auth_error_weak_password
            AuthError.NETWORK_ERROR -> GlobalR.string.auth_error_network
            AuthError.EMPTY_FIELDS -> GlobalR.string.auth_error_empty_fields
            AuthError.INVALID_EMAIL -> GlobalR.string.auth_error_invalid_email
            AuthError.PASSWORD_TOO_SHORT -> GlobalR.string.auth_error_password_too_short
            AuthError.NO_GOOGLE_ACCOUNTS -> GlobalR.string.auth_error_no_google_accounts
            AuthError.SESSION_EXPIRED -> GlobalR.string.auth_error_session_expired
            AuthError.ACCOUNT_DISABLED -> GlobalR.string.auth_error_account_disabled
            AuthError.SERVER_ERROR -> GlobalR.string.auth_error_server
        }
    )
