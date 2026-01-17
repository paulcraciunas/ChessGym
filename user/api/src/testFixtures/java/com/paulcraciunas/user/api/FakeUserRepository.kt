package com.paulcraciunas.user.api

import kotlinx.coroutines.flow.Flow

class FakeUserRepository(
    val local: FakeUserLocalDataSource = FakeUserLocalDataSource(),
    val remote: FakeUserRemoteDataSource = FakeUserRemoteDataSource(),
) : UserRepository {

    override fun userUpdates(): Flow<User> = local.userUpdates()

    override suspend fun get(): User = local.getUser()

    override suspend fun update(updated: User) {
        // Save to local storage first
        local.saveUser(updated)

        // Sync to remote if user is signed in
        if (updated.isSignedIn()) {
            remote.updateUser(updated)
        }
    }

    override suspend fun logHistory(history: List<User.HistoryItem>) {
        // Get current user and add history items
        val currentUser = get()
        val updatedUser = currentUser.copy(
            history = currentUser.history + history
        )

        // Save locally
        local.saveUser(updatedUser)

        // Sync to remote if signed in
        if (updatedUser.isSignedIn()) {
            remote.addToHistory(updatedUser.authentication!!.userId, history)
        }
    }

    override suspend fun signIn(auth: User.AuthenticationState, token: String): User {
        // Sign in with remote first
        val signedInUser = remote.signIn(auth, token)

        // Save the signed-in user locally
        local.saveUser(signedInUser)

        return signedInUser
    }

    override suspend fun signOut() {
        // Only clear local data - don't touch remote
        local.clearUserData()
    }

    override suspend fun clear() {
        val currentUser = get()

        currentUser.authentication?.userId?.let { remote.deleteUser(it) }
        local.clearUserData()
    }

    override suspend fun sync() {
        val currentUser = get()

        currentUser.authentication?.userId?.let { // Only sync if user is signed in
            local.saveUser(remote.getUser(it))
        }
    }
}
