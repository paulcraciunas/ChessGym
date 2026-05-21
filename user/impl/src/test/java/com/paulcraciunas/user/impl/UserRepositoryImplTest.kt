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
    fun `GIVEN auth result WHEN signIn THEN calls remote and merges with local`() = runBlocking {
        // Given
        val authResult = AuthResult(
            authState = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.APPLE,
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
        assertFalse(fakeSyncState.isDirty())
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
    fun `GIVEN signed in user and stale data WHEN sync THEN merges remote with local`() = runBlocking {
        // Given
        val user = UserTestFixtures.createSignedUpUser()
        val remoteUser = user.copy(
            profile = User.Profile(displayName = "RemoteUser"),
            failedPuzzles = emptyList(),
        )
        fakeLocalDataSource.saveUser(user)
        fakeRemoteDataSource.with(remoteUser)
        fakeSyncState.stale = true

        // When
        underTest.sync()

        // Then
        val synced = fakeLocalDataSource.getUser()
        assertEquals("RemoteUser", synced.profile.displayName)
        assertEquals(user.deviceId, synced.deviceId)
        assertEquals(user.history, synced.history)
        assertEquals(user.failedPuzzles, synced.failedPuzzles)
        assertEquals(user.authentication, synced.authentication)
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
