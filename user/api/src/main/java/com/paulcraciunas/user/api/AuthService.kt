package com.paulcraciunas.user.api

interface AuthService {
    suspend fun signInWithGoogleToken(idToken: String): AuthResult
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun signUpWithEmail(email: String, password: String): AuthResult
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun deleteAccount()
}
