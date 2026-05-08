package com.paulcraciunas.user.api

class FakeUserRemoteDataSource : UserRemoteDataSource {
    private var exception: Throwable? = null
    private var user: User? = null

    fun hasUser(): Boolean = user != null

    override suspend fun getUser(userId: String): User = if (user?.authentication?.userId == userId) {
        user!!
    } else {
        throw NoSuchElementException("No remote user found")
    }

    override suspend fun updateUser(user: User) {
        exception?.let { throw it }
        if (user.authentication?.userId != null && this.user?.authentication?.userId == user.authentication.userId) {
            this.user = user
        }
    }

    override suspend fun signIn(auth: User.AuthenticationState): User {
        exception?.let { throw it }
        user = User(authentication = auth)
        return user!!
    }

    override suspend fun deleteUser(userId: String) {
        exception?.let { throw it }
        if (this.user?.authentication?.userId == userId) {
            this.user = null
        }
    }

    fun with(with: User) = apply { this.user = with }
    fun failAll() = failAll(with = RuntimeException("Unexpected operation occurred"))
    fun failAll(with: Throwable) {
        exception = with
    }
}
