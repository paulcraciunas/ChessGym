package com.paulcraciunas.user.api

interface AuthService {
    suspend fun signInWithGoogleToken(idToken: String): User.AuthenticationState
    suspend fun signInWithEmail(email: String, password: String): User.AuthenticationState
    suspend fun signUpWithEmail(email: String, password: String): User.AuthenticationState
    suspend fun deleteAccount()
}
