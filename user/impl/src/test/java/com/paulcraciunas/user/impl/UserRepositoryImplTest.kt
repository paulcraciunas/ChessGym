package com.paulcraciunas.user.impl

import com.paulcraciunas.user.api.FakeSyncScheduler
import com.paulcraciunas.user.api.FakeSyncState
import com.paulcraciunas.user.api.AuthResult
import com.paulcraciunas.user.api.FakeUserLocalDataSource
import com.paulcraciunas.user.api.FakeUserRemoteDataSource
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class UserRepositoryImplTest {
    private val fakeLocalDataSource = FakeUserLocalDataSource()
    private val fakeRemoteDataSource = FakeUserRemoteDataSource()
    private val fakeSyncState = FakeSyncState()
    private val fakeSyncScheduler = FakeSyncScheduler()

    private val underTest = UserRepositoryImpl(
        fakeLocalDataSource,
        fakeRemoteDataSource,
        fakeSyncState,
        fakeSyncScheduler,
    )

    @Test
    fun `GIVEN local data source WHEN userUpdates THEN delegates to local data source`() = runBlocking {
        // Given
        val expectedUser = UserTestFixtures.createDefaultUser()
        fakeLocalDataSource.saveUser(expectedUser)

        // When
        val result = underTest.userUpdates().first()

        // Then
        assertEquals(expectedUser, result)
    }

    @Test
    fun `GIVEN local data source WHEN get THEN delegates to local data source`() = runBlocking {
        // Given
        val expectedUser = UserTestFixtures.createDefaultUser()
        fakeLocalDataSource.saveUser(expectedUser)

        // When
        val result = underTest.get()

        // Then
        assertEquals(expectedUser, result)
    }

    @Test
    fun `GIVEN unsigned user WHEN update THEN saves locally only and does not mark dirty`() = runBlocking {
        // Given
        val user = UserTestFixtures.createDefaultUser()

        // When
        fakeRemoteDataSource.failAll()
        underTest.update(user)

        // Then
        assertEquals(user, fakeLocalDataSource.getUser())
        assertFalse(fakeSyncState.isDirty())
        assertEquals(0, fakeSyncScheduler.scheduleCount)
    }

    @Test
    fun `GIVEN signed in user WHEN update THEN saves locally and marks dirty and enqueues worker`() = runBlocking {
        // Given
        val user = UserTestFixtures.createSignedUpUser()
        fakeRemoteDataSource.with(user)

        // When
        underTest.update(user)

        // Then
        assertEquals(user, fakeLocalDataSource.getUser())
        assertTrue(fakeSyncState.isDirty())
        assertEquals(1, fakeSyncScheduler.scheduleCount)
    }

    @Test
    fun `GIVEN unsigned user WHEN logHistory THEN adds to local history only`() = runBlocking {
        // Given
        val originalUser = UserTestFixtures.createDefaultUser()
        val newHistoryItems = listOf(UserTestFixtures.createSampleRatedPuzzleHistoryItem())
        fakeLocalDataSource.saveUser(originalUser)

        // When
        fakeRemoteDataSource.failAll()
        underTest.logHistory(newHistoryItems)

        // Then
        val updatedUser = fakeLocalDataSource.getUser()
        assertEquals(originalUser.history.size + 1, updatedUser.history.size)
        assertTrue(updatedUser.history.containsAll(originalUser.history))
        assertTrue(updatedUser.history.containsAll(newHistoryItems))
    }

    @Test
    fun `GIVEN signed in user WHEN logHistory THEN adds to local only`() = runBlocking {
        // Given
        val originalUser = UserTestFixtures.createSignedUpUser()
        val newHistoryItems = listOf(UserTestFixtures.createSampleRatedPuzzleHistoryItem())
        fakeLocalDataSource.saveUser(originalUser)

        // When
        fakeRemoteDataSource.failAll()
        underTest.logHistory(newHistoryItems)

        // Then
        val updatedUser = fakeLocalDataSource.getUser()
        assertEquals(originalUser.history.size + 1, updatedUser.history.size)
        assertTrue(updatedUser.history.containsAll(originalUser.history))
        assertTrue(updatedUser.history.containsAll(newHistoryItems))
    }

    @Test
    fun `GIVEN auth result WHEN signIn THEN preserves local progress over fresh remote defaults`() = runBlocking {
        // Given
        val authResult = AuthResult(
            authState = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
                userId = "user_123",
            ),
            displayName = "TestUser",
        )
        val localUser = UserTestFixtures.createDefaultUser()
        fakeLocalDataSource.saveUser(localUser)

        // When
        val result = underTest.signIn(authResult)

        // Then
        assertEquals(authResult.authState, result.authentication)
        assertEquals(localUser.deviceId, result.deviceId)
        assertEquals(localUser.history, result.history)
        assertEquals(localUser.failedPuzzles, result.failedPuzzles)
        assertEquals("TestUser", result.profile.displayName)
        assertEquals(localUser.ratings.current, result.ratings.current)
        assertEquals(localUser.highScores.ratedPuzzle, result.highScores.ratedPuzzle)
        assertEquals(localUser.highScores.puzzleRush, result.highScores.puzzleRush)
        assertEquals(localUser.statistics.puzzlesPlayed, result.statistics.puzzlesPlayed)
        assertEquals(localUser.statistics.puzzlesSolved, result.statistics.puzzlesSolved)
        assertFalse(fakeSyncState.isDirty())
    }

    @Test
    fun `GIVEN auth result WHEN signIn THEN pushes merged data to remote`() = runBlocking {
        // Given
        val authResult = AuthResult(
            authState = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
                userId = "user_123",
            ),
            displayName = "TestUser",
        )
        val localUser = UserTestFixtures.createDefaultUser()
        fakeLocalDataSource.saveUser(localUser)

        // When
        underTest.signIn(authResult)

        // Then
        val remoteUser = fakeRemoteDataSource.getUser("user_123")
        assertEquals(localUser.ratings.current, remoteUser.ratings.current)
        assertEquals(localUser.highScores.puzzleRush, remoteUser.highScores.puzzleRush)
        assertEquals(localUser.statistics.puzzlesPlayed, remoteUser.statistics.puzzlesPlayed)
    }

    @Test
    fun `GIVEN remote signIn fails WHEN signIn THEN propagates exception`() {
        runBlocking {
            // Given
            val authResult = AuthResult(
                authState = User.AuthenticationState(
                    provider = User.AuthenticationState.AuthProvider.GOOGLE,
                    userId = "user_123",
                ),
                displayName = null,
            )
            fakeRemoteDataSource.failAll(RuntimeException("Sign in failed"))

            // When & Then
            assertThrows<RuntimeException> { underTest.signIn(authResult) }
        }
    }

    @Test
    fun `GIVEN repository WHEN signOut THEN clears local data`() = runBlocking {
        // Given
        val signedInUser = UserTestFixtures.createSignedUpUser()
        fakeLocalDataSource.saveUser(signedInUser)

        // When
        underTest.signOut()

        // Then
        assertTrue(fakeLocalDataSource.getUser() != signedInUser)
    }

    @Test
    fun `GIVEN repository WHEN clear THEN clears local data and remote`() = runBlocking {
        // Given
        val signedInUser = UserTestFixtures.createSignedUpUser()
        fakeLocalDataSource.saveUser(signedInUser)
        fakeRemoteDataSource.with(signedInUser)

        // When
        underTest.clear()

        // Then
        assertTrue(fakeLocalDataSource.getUser() != signedInUser)
        assertFalse(fakeRemoteDataSource.hasUser())
    }

    @Test
    fun `GIVEN unsigned user WHEN sync THEN does not sync to remote`() = runBlocking {
        // Given
        val user = UserTestFixtures.createDefaultUser()
        fakeLocalDataSource.saveUser(user)

        // When
        fakeRemoteDataSource.failAll()
        underTest.sync()

        // Then
        assertFalse(fakeRemoteDataSource.hasUser())
    }

    @Test
    fun `GIVEN signed in user and stale data WHEN sync THEN merges with max-wins logic`() = runBlocking {
        // Given
        val user = UserTestFixtures.createSignedUpUser().copy(
            ratings = User.Ratings(current = 1200, blindMode = 600),
            highScores = User.HighScores(ratedPuzzle = 1200, puzzleRush = 20),
            statistics = User.Statistics(puzzlesPlayed = 100, puzzlesSolved = 80),
        )
        val remoteUser = user.copy(
            profile = User.Profile(displayName = "RemoteUser"),
            ratings = User.Ratings(current = 1400, blindMode = 500),
            highScores = User.HighScores(ratedPuzzle = 1400, puzzleRush = 10),
            statistics = User.Statistics(puzzlesPlayed = 90, puzzlesSolved = 85),
        )
        fakeLocalDataSource.saveUser(user)
        fakeRemoteDataSource.with(remoteUser)
        fakeSyncState.stale = true

        // When
        underTest.sync()

        // Then
        val synced = fakeLocalDataSource.getUser()
        assertEquals("RemoteUser", synced.profile.displayName)
        assertEquals(1400, synced.ratings.current)
        assertEquals(600, synced.ratings.blindMode)
        assertEquals(1400, synced.highScores.ratedPuzzle)
        assertEquals(20, synced.highScores.puzzleRush)
        assertEquals(100, synced.statistics.puzzlesPlayed)
        assertEquals(85, synced.statistics.puzzlesSolved)
        assertEquals(user.deviceId, synced.deviceId)
        assertEquals(user.history, synced.history)
        assertEquals(user.failedPuzzles, synced.failedPuzzles)
        assertEquals(user.authentication, synced.authentication)
    }

    @Test
    fun `GIVEN signed in user and dirty data WHEN sync THEN pushes local before pulling`() = runBlocking {
        // Given
        val user = UserTestFixtures.createSignedUpUser().copy(
            ratings = User.Ratings(current = 1500, blindMode = 600),
        )
        val remoteUser = user.copy(
            ratings = User.Ratings(current = 1200, blindMode = 400),
        )
        fakeLocalDataSource.saveUser(user)
        fakeRemoteDataSource.with(remoteUser)
        fakeSyncState.markDirty()

        // When
        underTest.sync()

        // Then — remote should have been updated with local's higher values
        val remoteAfterSync = fakeRemoteDataSource.getUser(user.authentication!!.userId)
        assertEquals(1500, remoteAfterSync.ratings.current)
        assertEquals(600, remoteAfterSync.ratings.blindMode)
        assertFalse(fakeSyncState.isDirty())
    }

    @Test
    fun `GIVEN signed in user and fresh data WHEN sync THEN does not fetch remote`() = runBlocking {
        // Given
        val user = UserTestFixtures.createSignedUpUser()
        fakeLocalDataSource.saveUser(user)
        fakeSyncState.markClean()

        // When
        fakeRemoteDataSource.failAll()
        underTest.sync()

        // Then
        assertEquals(user, fakeLocalDataSource.getUser())
    }

    @Test
    fun `GIVEN stale sync WHEN remote has mixed values THEN achievements use max-wins per key`() = runBlocking {
        // Given
        val user = UserTestFixtures.createSignedUpUser().copy(
            achievements = User.Achievements(
                progress = mapOf("RATING_CLIMBER" to 1500L, "PUZZLE_ADDICT" to 500L),
                lastActiveDate = LocalDate.of(2026, 5, 1),
                bestConsecutiveDaysStreak = 10,
                currentRatedWinStreak = 3,
                bestRatedWinStreak = 8,
            ),
        )
        val remoteUser = user.copy(
            achievements = User.Achievements(
                progress = mapOf("RATING_CLIMBER" to 1200L, "BLIND_STRATEGIST" to 600L),
                lastActiveDate = LocalDate.of(2026, 5, 15),
                bestConsecutiveDaysStreak = 15,
                currentRatedWinStreak = 1,
                bestRatedWinStreak = 12,
            ),
        )
        fakeLocalDataSource.saveUser(user)
        fakeRemoteDataSource.with(remoteUser)
        fakeSyncState.stale = true

        // When
        underTest.sync()

        // Then
        val synced = fakeLocalDataSource.getUser()
        assertEquals(1500L, synced.achievements.progress["RATING_CLIMBER"])
        assertEquals(500L, synced.achievements.progress["PUZZLE_ADDICT"])
        assertEquals(600L, synced.achievements.progress["BLIND_STRATEGIST"])
        assertEquals(LocalDate.of(2026, 5, 15), synced.achievements.lastActiveDate)
        assertEquals(15, synced.achievements.bestConsecutiveDaysStreak)
        assertEquals(3, synced.achievements.currentRatedWinStreak)
        assertEquals(12, synced.achievements.bestRatedWinStreak)
    }

    @Test
    fun `GIVEN same type same day history WHEN logHistory THEN merges items`() = runBlocking {
        // Given
        val today = LocalDate.now()
        val existingItem = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 3,
                puzzlesSolved = 2,
                ratingChange = 15,
                timeSpent = 1000L
            )
        )
        val user = User(history = listOf(existingItem))
        fakeLocalDataSource.saveUser(user)

        val newItem = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 2,
                puzzlesSolved = 1,
                ratingChange = 10,
                timeSpent = 500L
            )
        )

        // When
        underTest.logHistory(listOf(newItem))

        // Then
        val updatedUser = fakeLocalDataSource.getUser()
        assertEquals(1, updatedUser.history.size)
        val mergedData = updatedUser.history.first().data as User.HistoryItem.HistoryItemData.RatedPuzzleData
        assertEquals(5, mergedData.puzzlesPlayed)
        assertEquals(3, mergedData.puzzlesSolved)
        assertEquals(25, mergedData.ratingChange)
        assertEquals(1500L, mergedData.timeSpent)
    }

    @Test
    fun `GIVEN same type different day history WHEN logHistory THEN adds new item`() = runBlocking {
        // Given
        val yesterday = LocalDate.now().minusDays(1)
        val today = LocalDate.now()
        val existingItem = User.HistoryItem(
            timestamp = yesterday,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 3,
                puzzlesSolved = 2,
                ratingChange = 15,
                timeSpent = 1000L
            )
        )
        val user = User(history = listOf(existingItem))
        fakeLocalDataSource.saveUser(user)

        val newItem = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 2,
                puzzlesSolved = 1,
                ratingChange = 10,
                timeSpent = 500L
            )
        )

        // When
        underTest.logHistory(listOf(newItem))

        // Then
        val updatedUser = fakeLocalDataSource.getUser()
        assertEquals(2, updatedUser.history.size)
    }

    @Test
    fun `GIVEN different type same day history WHEN logHistory THEN adds new item`() = runBlocking {
        // Given
        val today = LocalDate.now()
        val existingItem = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                puzzlesPlayed = 3,
                puzzlesSolved = 2,
                ratingChange = 15,
                timeSpent = 1000L
            )
        )
        val user = User(history = listOf(existingItem))
        fakeLocalDataSource.saveUser(user)

        val newItem = User.HistoryItem(
            timestamp = today,
            data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                tries = 1,
                bestScore = 50,
                timeSpent = 500L
            )
        )

        // When
        underTest.logHistory(listOf(newItem))

        // Then
        val updatedUser = fakeLocalDataSource.getUser()
        assertEquals(2, updatedUser.history.size)
    }
}
