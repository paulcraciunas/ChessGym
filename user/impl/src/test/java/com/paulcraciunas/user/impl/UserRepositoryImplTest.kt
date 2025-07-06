package com.paulcraciunas.user.impl

import com.paulcraciunas.user.api.User
import com.paulcraciunas.user.api.UserLocalDataSource
import com.paulcraciunas.user.api.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class UserRepositoryImplTest {
    private val fakeLocalDataSource = FakeUserLocalDataSource()
    private val fakeRemoteDataSource = FakeUserRemoteDataSource()

    private val underTest = UserRepositoryImpl(fakeLocalDataSource, fakeRemoteDataSource)

    @Test
    fun given_localDataSource_WHEN_userUpdates_THEN_delegatesToLocalDataSource() = runBlocking {
        // Given
        val expectedUser = UserTestFixtures.createDefaultUser()
        fakeLocalDataSource.saveUser(expectedUser)

        // When
        val result = underTest.userUpdates().first()

        // Then
        assertEquals(expectedUser, result)
    }

    @Test
    fun given_localDataSource_WHEN_get_THEN_delegatesToLocalDataSource() = runBlocking {
        // Given
        val expectedUser = UserTestFixtures.createDefaultUser()
        fakeLocalDataSource.saveUser(expectedUser)

        // When
        val result = underTest.get()

        // Then
        assertEquals(expectedUser, result)
    }

    @Test
    fun given_unsignedUser_WHEN_update_THEN_savesLocallyOnly() = runBlocking {
        // Given
        val user = UserTestFixtures.createDefaultUser() // Not signed in

        // When
        fakeRemoteDataSource.failAll()
        underTest.update(user)

        // Then
        assertEquals(user, fakeLocalDataSource.getUser())
    }

    @Test
    fun given_signedInUser_WHEN_update_THEN_savesLocallyAndSyncsRemote() = runBlocking {
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

    @Test(expected = RuntimeException::class)
    fun given_remoteDataSourceThrows_WHEN_updateSignedInUser_THEN_propagatesException() {
        runBlocking {
            // Given
            val user = UserTestFixtures.createSignedUpUser()
            fakeRemoteDataSource.failAll(RuntimeException("Update failed"))

            // When & Then
            underTest.update(user) // Should throw
        }
    }

    @Test
    fun given_unsignedUser_WHEN_logHistory_THEN_addsToLocalHistoryOnly() = runBlocking {
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
    fun given_signedInUser_WHEN_logHistory_THEN_addsToLocalAndSyncsRemote() = runBlocking {
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
    fun given_authStateAndToken_WHEN_signIn_THEN_callsRemoteAndSavesLocally() = runBlocking {
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

    @Test(expected = RuntimeException::class)
    fun given_remoteSignInFails_WHEN_signIn_THEN_propagatesException() {
        runBlocking {
            // Given
            val authState = User.AuthenticationState(
                provider = User.AuthenticationState.AuthProvider.GOOGLE,
                userId = "user_123"
            )
            fakeRemoteDataSource.failAll(RuntimeException("Sign in failed"))

            // When & Then
            underTest.signIn(authState, "auth_token") // Should throw
        }
    }

    @Test
    fun given_repository_WHEN_signOut_THEN_clearsLocalDataOnly() = runBlocking {
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
    fun given_repository_WHEN_clear_THEN_clearsLocalDataAndRemote() = runBlocking {
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
    fun given_unsignedUser_WHEN_sync_THEN_doesNotSyncToRemote() = runBlocking {
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
    fun given_signedInUser_WHEN_sync_THEN_syncsToRemote() = runBlocking {
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
}

// Fake implementations for testing
private class FakeUserLocalDataSource : UserLocalDataSource {
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

private class FakeUserRemoteDataSource : UserRemoteDataSource {
    private var exception: Throwable? = null
    private var user: User? = null

    fun hasUser(): Boolean = user != null

    override suspend fun getUser(userId: String): User = if (user?.authentication?.userId == userId) {
        user!!
    } else {
        throw NoSuchElementException("No remote user found")
    }

    override suspend fun updateUser(user: User) {
        exception?.let { throw it }
        if (user.authentication?.userId != null && this.user?.authentication?.userId == user.authentication?.userId) {
            this.user = user
        }
    }

    override suspend fun addToHistory(userId: String, history: List<User.HistoryItem>) {
        exception?.let { throw it }
        user?.let { user ->
            user.authentication?.userId?.let {
                if (it == userId) {
                    this.user = user.copy(history = user.history.toMutableList().apply { addAll(history) })
                }
            }
        }
    }

    override suspend fun signIn(auth: User.AuthenticationState, token: String): User {
        exception?.let { throw it }
        user = User(authentication = auth)
        return user!!
    }

    override suspend fun deleteUser(userId: String) {
        exception?.let { throw it }
        if (this.user?.authentication?.userId == userId) {
            this.user = null
        }
    }

    fun with(with: User) = apply { this.user = with }
    fun failAll() = failAll(with = RuntimeException("Unexpected operation occurred"))
    fun failAll(with: Throwable) {
        exception = with
    }
}
