package com.paulcraciunas.user.api

class FakeAuthService : AuthService {
    private val users: MutableMap<String, StoredUser> = mutableMapOf()
    private val userTokens: MutableMap<String, TokenUser> = mutableMapOf()
    private var genericError: Throwable? = null

    var defaultUserId: String = UserDefaults.USER_ID

    override suspend fun signInWithGoogleToken(idToken: String): AuthResult {
        genericError?.let { throw it }
        val tokenUser = userTokens[idToken] ?: throw AuthException.UserNotFound()
        return AuthResult(
            authState = User.AuthenticationState(
                userId = tokenUser.id,
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
            ),
            displayName = tokenUser.displayName,
        )
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        genericError?.let { throw it }
        if (!users.contains(email)) throw AuthException.UserNotFound()
        if (users[email]!!.password != password) throw AuthException.InvalidCredentials()
        return AuthResult(
            authState = User.AuthenticationState(
                userId = users[email]!!.id,
                provider = User.AuthenticationState.AuthProvider.EMAIL,
            ),
            displayName = users[email]?.name,
        )
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        genericError?.let { throw it }
        if (password.length <= 6) throw AuthException.WeakPassword()
        if (users.contains(email)) throw AuthException.AccountCollision()
        users[email] = StoredUser(defaultUserId, password)
        return AuthResult(
            authState = User.AuthenticationState(
                userId = users[email]!!.id,
                provider = User.AuthenticationState.AuthProvider.EMAIL,
            ),
            displayName = null,
        )
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        genericError?.let { throw it }
        if (!users.contains(email)) throw AuthException.UserNotFound()
    }

    override suspend fun deleteAccount() {
        genericError?.let { throw it }
        users.clear()
        userTokens.clear()
    }

    fun isClear(): Boolean = users.isEmpty() && userTokens.isEmpty()

    fun withExistingUser(email: String, password: String, id: String, name: String? = null): FakeAuthService = apply {
        users[email] = StoredUser(id = id, password = password, name)
    }

    fun withTokenUser(token: String, id: String, displayName: String? = null): FakeAuthService = apply {
        userTokens[token] = TokenUser(id = id, displayName = displayName)
    }

    fun disconnect(): FakeAuthService = apply {
        genericError = AuthException.NetworkError()
    }

    fun withError(e: Throwable) {
        genericError = e
    }

    private class StoredUser(val id: String, val password: String, val name: String? = null)
    private class TokenUser(val id: String, val displayName: String?)
}
