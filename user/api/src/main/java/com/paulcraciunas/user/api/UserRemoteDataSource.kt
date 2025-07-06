package com.paulcraciunas.user.api

interface UserRemoteDataSource {
    suspend fun getUser(userId: String): User
    suspend fun updateUser(user: User)

    suspend fun addToHistory(userId: String, history: List<User.HistoryItem>)

    suspend fun signIn(auth: User.AuthenticationState, token: String): User
    suspend fun deleteUser(userId: String)
}
