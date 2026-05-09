package com.paulcraciunas.user.api

sealed class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidCredentials(cause: Throwable? = null) : AuthException("Invalid credentials", cause)
    class UserNotFound(cause: Throwable? = null) : AuthException("User not found", cause)
    class AccountCollision(cause: Throwable? = null) : AuthException("Account already exists", cause)
    class WeakPassword(cause: Throwable? = null) : AuthException("Password too weak", cause)
    class NetworkError(cause: Throwable? = null) : AuthException("Network error", cause)
    class Unknown(cause: Throwable? = null) : AuthException("Unknown auth error", cause)
    class NoCredentials(cause: Throwable? = null) : AuthException("No credentials", cause)
}
