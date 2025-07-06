package com.paulcraciunas.user.impl

import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserLocalDataSource
import com.paulcraciunas.user.api.UserRemoteDataSource
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {

    override fun userUpdates(): Flow<User> = localDataSource.userUpdates()

    override suspend fun get(): User = localDataSource.getUser()

    override suspend fun update(updated: User) {
        // Save to local storage first
        localDataSource.saveUser(updated)

        // Sync to remote if user is signed in
        if (updated.isSignedIn()) {
            remoteDataSource.updateUser(updated)
        }
    }

    override suspend fun logHistory(history: List<User.HistoryItem>) {
        // Get current user and add history items
        val currentUser = get()
        val updatedUser = currentUser.copy(
            history = currentUser.history + history
        )

        // Save locally
        localDataSource.saveUser(updatedUser)

        // Sync to remote if signed in
        if (updatedUser.isSignedIn()) {
            remoteDataSource.addToHistory(updatedUser.authentication!!.userId, history)
        }
    }

    override suspend fun signIn(auth: User.AuthenticationState, token: String): User {
        // Sign in with remote first
        val signedInUser = remoteDataSource.signIn(auth, token)

        // Save the signed-in user locally
        localDataSource.saveUser(signedInUser)

        return signedInUser
    }

    override suspend fun signOut() {
        // Only clear local data - don't touch remote
        localDataSource.clearUserData()
    }

    override suspend fun clear() {
        val currentUser = get()

        currentUser.authentication?.userId?.let { remoteDataSource.deleteUser(it) }
        localDataSource.clearUserData()
    }

    override suspend fun sync() {
        val currentUser = get()

        currentUser.authentication?.userId?.let { // Only sync if user is signed in
            localDataSource.saveUser(remoteDataSource.getUser(it))
        }
    }
}
