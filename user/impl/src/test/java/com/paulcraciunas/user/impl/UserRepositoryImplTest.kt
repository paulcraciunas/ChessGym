package com.paulcraciunas.user.impl

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

    private val underTest = UserRepositoryImpl(fakeLocalDataSource, fakeRemoteDataSource)

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
    fun `GIVEN unsigned user WHEN update THEN saves locally only`() = runBlocking {
        // Given
        val user = UserTestFixtures.createDefaultUser() // Not signed in

        // When
        fakeRemoteDataSource.failAll()
        underTest.update(user)

        // Then
        assertEquals(user, fakeLocalDataSource.getUser())
    }

    @Test
    fun `GIVEN signed in user WHEN update THEN saves locally and syncs remote`() = runBlocking {
        // Given
        var user = UserTestFixtures.createSignedUpUser()
        fakeRemoteDataSource.with(user)

        // When
        user = user.copy(failedPuzzles = emptyList())
        underTest.update(user)

        // Then
        assertEquals(user, fakeLocalDataSource.getUser())
        assertEquals(user, fakeRemoteDataSource.getUser(user.authentication!!.userId))
    }

    @Test
    fun `GIVEN remote data source throws WHEN update signed in user THEN propagates exception`() {
        runBlocking {
            // Given
            val user = UserTestFixtures.createSignedUpUser()
            fakeRemoteDataSource.failAll(RuntimeException("Update failed"))

            // When & Then
            assertThrows<RuntimeException> { underTest.update(user) }
        }
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
    fun `GIVEN signed in user WHEN logHistory THEN adds to local and syncs remote`() = runBlocking {
        // Given
        val originalUser = UserTestFixtures.createSignedUpUser()
        val newHistoryItems = listOf(UserTestFixtures.createSampleRatedPuzzleHistoryItem())
        fakeLocalDataSource.saveUser(originalUser)
        fakeRemoteDataSource.with(originalUser)

        // When
        underTest.logHistory(newHistoryItems)

        // Then
        val updatedUser = fakeLocalDataSource.getUser()
        val updatedRemoteUser = fakeRemoteDataSource.getUser(originalUser.authentication!!.userId)
        assertEquals(originalUser.history.size + 1, updatedUser.history.size)
        assertEquals(originalUser.history.size + 1, updatedRemoteUser.history.size)
        assertTrue(updatedUser.history.containsAll(originalUser.history))
        assertTrue(updatedUser.history.containsAll(newHistoryItems))
        assertTrue(updatedRemoteUser.history.containsAll(originalUser.history))
        assertTrue(updatedRemoteUser.history.containsAll(newHistoryItems))
    }

    @Test
    fun `GIVEN auth state and token WHEN signIn THEN calls remote and saves locally`() = runBlocking {
        // Given
        val authState = User.AuthenticationState(
            provider = User.AuthenticationState.AuthProvider.APPLE,
            userId = "user_123"
        )
        val token = "auth_token"
        val signedInUser = User().copy(authentication = authState)

        // When
        val result = underTest.signIn(authState, token)

        // Then
        assertEquals(signedInUser, result)
        assertEquals(signedInUser, fakeLocalDataSource.getUser())
        assertEquals(signedInUser, fakeRemoteDataSource.getUser("user_123"))
    }

    @Test
    fun `GIVEN remote signIn fails WHEN signIn THEN propagates exception`() {
        runBlocking {
            // Given
            val authState = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
                userId = "user_123"
            )
            fakeRemoteDataSource.failAll(RuntimeException("Sign in failed"))

            // When & Then
            assertThrows<RuntimeException> { underTest.signIn(authState, "auth_token") }
        }
    }

    @Test
    fun `GIVEN repository WHEN signOut THEN clears local data only`() = runBlocking {
        // Given
        val signedInUser = UserTestFixtures.createSignedUpUser()
        fakeLocalDataSource.saveUser(signedInUser)

        // When
        fakeRemoteDataSource.failAll()
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
        val user = UserTestFixtures.createDefaultUser() // Not signed in
        fakeLocalDataSource.saveUser(user)

        // When
        fakeRemoteDataSource.failAll()
        underTest.sync()

        // Then
        assertFalse(fakeRemoteDataSource.hasUser())
    }

    @Test
    fun `GIVEN signed in user WHEN sync THEN syncs to remote`() = runBlocking {
        // Given
        val user = UserTestFixtures.createSignedUpUser()
        val updated = user.copy(failedPuzzles = emptyList())
        fakeLocalDataSource.saveUser(user)
        fakeRemoteDataSource.with(updated)

        // When
        underTest.sync()

        // Then
        assertEquals(updated, fakeRemoteDataSource.getUser(user.authentication!!.userId))
        assertEquals(updated, fakeLocalDataSource.getUser())
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
