package com.paulcraciunas.user.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeUserLocalDataSource : UserLocalDataSource {
    private val _user = MutableStateFlow(User())

    override fun userUpdates(): Flow<User> = _user
    override suspend fun getUser(): User = _user.value
    override suspend fun saveUser(user: User) {
        _user.value = user
    }

    override suspend fun updateUser(updater: (User) -> User) {
        _user.update(updater)
    }

    override suspend fun clearUserData() {
        _user.value = User()
    }
}
