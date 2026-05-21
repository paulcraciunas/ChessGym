package com.paulcraciunas.user.impl

import com.paulcraciunas.user.api.AuthResult
import com.paulcraciunas.user.api.SyncScheduler
import com.paulcraciunas.user.api.SyncState
import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserLocalDataSource
import com.paulcraciunas.user.api.UserRemoteDataSource
import com.paulcraciunas.user.api.UserRepository
import com.paulcraciunas.user.api.canMergeWith
import com.paulcraciunas.user.api.mergeWith
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource,
    private val syncState: SyncState,
    private val syncScheduler: SyncScheduler,
) : UserRepository {

    override fun userUpdates(): Flow<User> = localDataSource.userUpdates()

    override suspend fun get(): User = localDataSource.getUser()

    override suspend fun update(updated: User) {
        localDataSource.saveUser(updated)

        if (updated.isSignedIn()) {
            syncState.markDirty()
            syncScheduler.schedule()
        }
    }

    override suspend fun logHistory(history: List<User.HistoryItem>) {
        val currentUser = get()
        val mergedHistory = mergeHistory(currentUser.history, history)
        val updatedUser = currentUser.copy(history = mergedHistory)
        localDataSource.saveUser(updatedUser)
    }

    override suspend fun signIn(authResult: AuthResult): User {
        val localUser = get()
        val remoteUser = remoteDataSource.signIn(authResult, localUser.deviceId)
        val merged = mergeWithRemote(localUser, remoteUser).copy(authentication = authResult.authState)
        localDataSource.saveUser(merged)
        syncState.markClean()
        return merged
    }

    override suspend fun signOut() {
        localDataSource.clearUserData()
    }

    override suspend fun clear() {
        val currentUser = get()
        currentUser.authentication?.userId?.let { remoteDataSource.deleteUser(it) }
        localDataSource.clearUserData()
    }

    override suspend fun sync() {
        val currentUser = get()
        if (!currentUser.isSignedIn()) return

        val userId = currentUser.authentication!!.userId
        val shouldSync = syncState.isDirty() || syncState.isStale()

        if (!shouldSync) return

        try {
            val remoteUser = remoteDataSource.getUser(userId)
            val merged = mergeWithRemote(currentUser, remoteUser)
            localDataSource.saveUser(merged)
            syncState.markClean()
        } catch (e: Exception) {
            Timber.w(e, "Sync failed, will retry on next opportunity")
        }
    }

    private fun mergeWithRemote(local: User, remote: User): User = local.copy(
        profile = remote.profile,
        ratings = local.ratings.copy(
            current = remote.ratings.current,
            blindMode = remote.ratings.blindMode,
        ),
        highScores = remote.highScores,
        statistics = remote.statistics,
        achievements = local.achievements.copy(
            progress = remote.achievements.progress,
            lastActiveDate = remote.achievements.lastActiveDate,
            consecutiveDaysStreak = remote.achievements.consecutiveDaysStreak,
            bestConsecutiveDaysStreak = remote.achievements.bestConsecutiveDaysStreak,
            currentRatedWinStreak = remote.achievements.currentRatedWinStreak,
            bestRatedWinStreak = remote.achievements.bestRatedWinStreak,
        ),
    )

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
}
