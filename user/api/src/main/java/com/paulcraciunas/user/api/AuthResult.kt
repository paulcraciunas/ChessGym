package com.paulcraciunas.user.api

data class AuthResult(
    val authState: User.AuthenticationState,
    val displayName: String?,
)
