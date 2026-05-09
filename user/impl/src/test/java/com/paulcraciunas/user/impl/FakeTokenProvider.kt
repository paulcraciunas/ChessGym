package com.paulcraciunas.user.impl

import com.paulcraciunas.user.api.TokenProvider

internal class FakeTokenProvider : TokenProvider {
    private var token: String? = "fake-token"

    override suspend fun getToken(forceRefresh: Boolean): String? = token

    override fun signOut() {
        token = null
    }
}
