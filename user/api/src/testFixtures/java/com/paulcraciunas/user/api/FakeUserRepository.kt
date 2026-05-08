package com.paulcraciunas.user.api

import kotlinx.coroutines.flow.Flow

class FakeUserRepository(
    val local: FakeUserLocalDataSource = FakeUserLocalDataSource(),
    val remote: FakeUserRemoteDataSource = FakeUserRemoteDataSource(),
) : UserRepository {

    override fun userUpdates(): Flow<User> = local.userUpdates()

    override suspend fun get(): User = local.getUser()

    override suspend fun update(updated: User) {
        local.saveUser(updated)

        if (updated.isSignedIn()) {
            remote.updateUser(updated)
        }
    }

    override suspend fun logHistory(history: List<User.HistoryItem>) {
        val currentUser = get()
        val mergedHistory = mergeHistory(currentUser.history, history)
        val updatedUser = currentUser.copy(history = mergedHistory)
        local.saveUser(updatedUser)
    }

    private fun mergeHistory(
        existing: List<User.HistoryItem>,
        new: List<User.HistoryItem>
    ): List<User.HistoryItem> {
        val result = existing.toMutableList()
        for (newItem in new) {
            val existingIndex = result.indexOfFirst { it.canMergeWith(newItem) }
            if (existingIndex >= 0) {
                result[existingIndex] = result[existingIndex].mergeWith(newItem)
            } else {
                result.add(newItem)
            }
        }
        return result
    }

    override suspend fun signIn(auth: User.AuthenticationState): User {
        val signedInUser = remote.signIn(auth)
        local.saveUser(signedInUser)
        return signedInUser
    }

    override suspend fun signOut() {
        local.clearUserData()
    }

    override suspend fun clear() {
        val currentUser = get()
        currentUser.authentication?.userId?.let { remote.deleteUser(it) }
        local.clearUserData()
    }

    override suspend fun sync() {
        val currentUser = get()
        currentUser.authentication?.userId?.let {
            local.saveUser(remote.getUser(it))
        }
    }
}
