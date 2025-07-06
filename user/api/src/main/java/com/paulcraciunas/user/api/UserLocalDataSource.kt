package com.paulcraciunas.user.api

import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    fun userUpdates(): Flow<User>

    suspend fun getUser(): User
    suspend fun saveUser(user: User)
    suspend fun updateUser(updater: (User) -> User)
    suspend fun clearUserData()
}
