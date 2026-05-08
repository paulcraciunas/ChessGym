package com.paulcraciunas.user.api

interface TokenProvider {
    suspend fun getToken(forceRefresh: Boolean = false): String?
    fun signOut()
}
