package com.paulcraciunas.user.api

class FakeTokenProvider : TokenProvider {
    private var token: String? = "fake-token"
    var isSignedOut: Boolean = false
        private set

    override suspend fun getToken(forceRefresh: Boolean): String? = token

    override fun signOut() {
        token = null
        isSignedOut = true
    }
}
