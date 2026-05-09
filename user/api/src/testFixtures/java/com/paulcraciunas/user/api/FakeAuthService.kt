package com.paulcraciunas.user.api

class FakeAuthService : AuthService {
    private val users: MutableMap<String, StoredUser> = mutableMapOf()
    private val userTokens: MutableMap<String, String> = mutableMapOf()
    private var genericError: AuthException? = null

    var defaultUserId: String = UserDefaults.USER_ID

    override suspend fun signInWithGoogleToken(idToken: String): User.AuthenticationState {
        genericError?.let { throw it }
        val userId = userTokens[idToken] ?: throw AuthException.UserNotFound()
        return User.AuthenticationState(
            userId = userId,
            provider = User.AuthenticationState.AuthProvider.GOOGLE,
        )
    }

    override suspend fun signInWithEmail(email: String, password: String): User.AuthenticationState {
        genericError?.let { throw it }
        if (!users.contains(email)) throw AuthException.UserNotFound()
        if (users[email]!!.password != password) throw AuthException.InvalidCredentials()
        return User.AuthenticationState(userId = users[email]!!.id, provider = User.AuthenticationState.AuthProvider.EMAIL)
    }

    override suspend fun signUpWithEmail(email: String, password: String): User.AuthenticationState {
        genericError?.let { throw it }
        if (password.length <= 6) throw AuthException.WeakPassword()
        if (users.contains(email)) throw AuthException.AccountCollision()
        users[email] = StoredUser(defaultUserId, password)
        return User.AuthenticationState(userId = users[email]!!.id, provider = User.AuthenticationState.AuthProvider.EMAIL)
    }

    override suspend fun deleteAccount() {
        genericError?.let { throw it }

        users.clear()
        userTokens.clear()
    }

    fun isClear() = users.isEmpty() && userTokens.isEmpty()

    fun withExistingUser(email: String, password: String, id: String): FakeAuthService = apply {
        users[email] = StoredUser(id, password)
    }

    fun withExistingUser(token: String, id: String): FakeAuthService = apply {
        userTokens[token] = id
    }

    fun disconnect(): FakeAuthService = apply {
        genericError = AuthException.NetworkError()
    }

    private class StoredUser(val id: String, val password: String)
}
