package com.paulcraciunas.user.remote

import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

// No-op implementation
@Singleton
class UserRemoteDataSourceImpl @Inject constructor() : UserRemoteDataSource {
    override suspend fun getUser(userId: String) = throw up
    override suspend fun updateUser(user: User) = throw up
    override suspend fun addToHistory(userId: String, history: List<User.HistoryItem>) = throw up

    override suspend fun signIn(auth: User.AuthenticationState, token: String): User = throw up
    override suspend fun deleteUser(userId: String) = throw up

    companion object {
        // I'm being funny :D
        private val up = NotImplementedError("Remote data source not implemented yet")
    }
}
