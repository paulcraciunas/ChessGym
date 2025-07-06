package com.paulcraciunas.user.api

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun userUpdates(): Flow<User>
    suspend fun get(): User

    suspend fun update(updated: User)
    suspend fun logHistory(history: List<User.HistoryItem>)

    suspend fun signIn(auth: User.AuthenticationState, token: String): User
    suspend fun signOut()

    suspend fun clear()
    suspend fun sync()
}
