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
        remoteDataSource.updateUser(merged)
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
        if (!syncState.isDirty() && !syncState.isStale()) return

        try {
            if (syncState.isDirty()) {
                remoteDataSource.updateUser(currentUser)
            }
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
            current = maxOf(local.ratings.current, remote.ratings.current),
            blindMode = maxOf(local.ratings.blindMode, remote.ratings.blindMode),
        ),
        highScores = mergeHighScores(local.highScores, remote.highScores),
        statistics = mergeStatistics(local.statistics, remote.statistics),
        achievements = mergeAchievements(local.achievements, remote.achievements),
    )

    private fun mergeHighScores(
        local: User.HighScores,
        remote: User.HighScores,
    ): User.HighScores = User.HighScores(
        ratedPuzzle = maxOf(local.ratedPuzzle, remote.ratedPuzzle),
        puzzleRush = maxOf(local.puzzleRush, remote.puzzleRush),
        puzzleStreak = maxOf(local.puzzleStreak, remote.puzzleStreak),
        findTheSquare = maxOf(local.findTheSquare, remote.findTheSquare),
        knightPath = maxOf(local.knightPath, remote.knightPath),
        blindMode = maxOf(local.blindMode, remote.blindMode),
    )

    private fun mergeStatistics(
        local: User.Statistics,
        remote: User.Statistics,
    ): User.Statistics = User.Statistics(
        puzzlesPlayed = maxOf(local.puzzlesPlayed, remote.puzzlesPlayed),
        puzzlesSolved = maxOf(local.puzzlesSolved, remote.puzzlesSolved),
        totalTimeSpent = maxOf(local.totalTimeSpent, remote.totalTimeSpent),
        ratedPuzzlesSolved = maxOf(local.ratedPuzzlesSolved, remote.ratedPuzzlesSolved),
        puzzleRushSessions = maxOf(local.puzzleRushSessions, remote.puzzleRushSessions),
        streakSessions = maxOf(local.streakSessions, remote.streakSessions),
        failedPuzzlesRedeemed = maxOf(local.failedPuzzlesRedeemed, remote.failedPuzzlesRedeemed),
        findSquareSessions = maxOf(local.findSquareSessions, remote.findSquareSessions),
        knightPathSessions = maxOf(local.knightPathSessions, remote.knightPathSessions),
        blindModeWins = maxOf(local.blindModeWins, remote.blindModeWins),
        rushPuzzlesSolved = maxOf(local.rushPuzzlesSolved, remote.rushPuzzlesSolved),
    )

    private fun mergeAchievements(
        local: User.Achievements,
        remote: User.Achievements,
    ): User.Achievements {
        val mergedProgress = (local.progress.keys + remote.progress.keys)
            .associateWith { key ->
                maxOf(local.progress[key] ?: 0L, remote.progress[key] ?: 0L)
            }
        val mergedLastActive = listOfNotNull(local.lastActiveDate, remote.lastActiveDate).maxOrNull()
        return local.copy(
            progress = mergedProgress,
            lastActiveDate = mergedLastActive,
            consecutiveDaysStreak = maxOf(local.consecutiveDaysStreak, remote.consecutiveDaysStreak),
            bestConsecutiveDaysStreak = maxOf(local.bestConsecutiveDaysStreak, remote.bestConsecutiveDaysStreak),
            currentRatedWinStreak = maxOf(local.currentRatedWinStreak, remote.currentRatedWinStreak),
            bestRatedWinStreak = maxOf(local.bestRatedWinStreak, remote.bestRatedWinStreak),
        )
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
}
