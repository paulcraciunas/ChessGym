package com.paulcraciunas.user.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserLocalDataSource : UserLocalDataSource {
    private var user = User()

    override fun userUpdates(): Flow<User> = flowOf(user)
    override suspend fun getUser(): User = user
    override suspend fun saveUser(user: User) {
        this.user = user
    }

    override suspend fun updateUser(updater: (User) -> User) {
        this.user = updater(this.user)
    }

    override suspend fun clearUserData() {
        this.user = User()
    }
}
