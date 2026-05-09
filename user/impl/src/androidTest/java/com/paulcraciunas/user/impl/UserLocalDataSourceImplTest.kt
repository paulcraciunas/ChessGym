package com.paulcraciunas.user.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
internal class UserLocalDataSourceImplTest {

    private companion object {
        const val TEST_DEVICE_ID = "test-device-id"
    }

    private lateinit var context: Context
    private lateinit var underTest: UserLocalDataSourceImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        underTest = UserLocalDataSourceImpl(context)
    }

    @After
    fun tearDown() = runBlocking {
        underTest.clearUserData()
    }

    @Test
    fun given_emptyDataStore_WHEN_getUser_THEN_returnsDefaultUser() = runBlocking {
        // When
        val result = underTest.getUser()

        // Then
        assertTrue(result.deviceId.isNotEmpty())
        assertEquals(User(deviceId = result.deviceId), result)
    }

    @Test
    fun given_emptyDataStore_WHEN_userUpdates_THEN_emitsDefaultUser() = runBlocking {
        // When
        val result = underTest.userUpdates().first()

        // Then
        assertEquals(User(), result)
    }

    @Test
    fun given_simpleUser_WHEN_saveAndGet_THEN_returnsCorrectUser() = runBlocking {
        // Given
        val user = User(
            deviceId = TEST_DEVICE_ID,
            profile = User.Profile(
                firstName = "John",
                lastName = "Doe",
                joinDate = LocalDate.of(2023, 5, 15),
                avatarUrl = "https://example.com/avatar.jpg"
            ),
            ratings = User.Ratings(
                current = 1450,
                blindMode = 600
            )
        )

        // When
        underTest.saveUser(user)
        val result = underTest.getUser()

        // Then
        assertEquals(user, result)
    }

    @Test
    fun given_complexUserWithAllData_WHEN_saveAndGet_THEN_allDataSerializedCorrectly() = runBlocking {
        // Given
        val complexUser = createComplexUser()

        // When
        underTest.saveUser(complexUser)
        val result = underTest.getUser()

        // Then
        assertEquals(complexUser, result)

        assertEquals("Alice", result.profile.firstName)
        assertEquals("Smith", result.profile.lastName)
        assertEquals(LocalDate.of(2022, 3, 10), result.profile.joinDate)
        assertEquals("https://example.com/alice.jpg", result.profile.avatarUrl)

        assertEquals(1650, result.ratings.current)
        assertEquals(800, result.ratings.blindMode)

        assertEquals(1650, result.highScores.ratedPuzzle)
        assertEquals(120, result.highScores.puzzleRush)
        assertEquals(800, result.highScores.blindMode)

        assertEquals(250, result.statistics.puzzlesPlayed)
        assertEquals(200, result.statistics.puzzlesSolved)
        assertEquals(7200000L, result.statistics.totalTimeSpent)

        assertEquals(4, result.history.size)
        assertEquals(listOf(42, 137, 289), result.failedPuzzles)

        assertEquals(User.AuthenticationState.AuthProvider.GOOGLE, result.authentication?.provider)
        assertEquals("user_alice_123", result.authentication?.userId)
    }

    @Test
    fun given_userWithHistoryItems_WHEN_saveAndGet_THEN_sealedClassesSerializedCorrectly() = runBlocking {
        // Given
        val historyItems = listOf(
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 15),
                data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                    tries = 5,
                    bestScore = 95,
                    timeSpent = 1800000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 14),
                data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                    sessionsCompleted = 3,
                    timeSpent = 900000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 13),
                data = User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                    tries = 2,
                    mostMovesCompleted = 18,
                    timeSpent = 1200000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 12),
                data = User.HistoryItem.HistoryItemData.BlindModeData(
                    played = 1,
                    ratingChange = 45,
                    timeSpent = 600000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 11),
                data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                    puzzlesPlayed = 15,
                    puzzlesSolved = 12,
                    ratingChange = 25,
                    timeSpent = 2100000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 10),
                data = User.HistoryItem.HistoryItemData.PuzzleStreakData(
                    finalStreakCount = 12,
                    timeSpent = 3600000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 9),
                data = User.HistoryItem.HistoryItemData.FailedPuzzleData(
                    puzzlesSolved = 4,
                    timeSpent = 1000000L
                )
            )
        )

        val user = User(deviceId = TEST_DEVICE_ID, history = historyItems)

        // When
        underTest.saveUser(user)
        val result = underTest.getUser()

        // Then
        assertEquals(user, result)
        assertEquals(7, result.history.size)

        val puzzleRushItem = result.history[0].data as User.HistoryItem.HistoryItemData.PuzzleRushData
        assertEquals(5, puzzleRushItem.tries)
        assertEquals(95, puzzleRushItem.bestScore)
        assertEquals(1800000L, puzzleRushItem.timeSpent)

        val boardVizItem = result.history[1].data as User.HistoryItem.HistoryItemData.BoardVisualizationData
        assertEquals(3, boardVizItem.sessionsCompleted)
        assertEquals(900000L, boardVizItem.timeSpent)

        val blindTrainingItem = result.history[2].data as User.HistoryItem.HistoryItemData.BlindModeTrainingData
        assertEquals(2, blindTrainingItem.tries)
        assertEquals(18, blindTrainingItem.mostMovesCompleted)
        assertEquals(1200000L, blindTrainingItem.timeSpent)

        val blindModeItem = result.history[3].data as User.HistoryItem.HistoryItemData.BlindModeData
        assertEquals(1, blindModeItem.played)
        assertEquals(45, blindModeItem.ratingChange)
        assertEquals(600000L, blindModeItem.timeSpent)

        val ratedPuzzleItem = result.history[4].data as User.HistoryItem.HistoryItemData.RatedPuzzleData
        assertEquals(15, ratedPuzzleItem.puzzlesPlayed)
        assertEquals(12, ratedPuzzleItem.puzzlesSolved)
        assertEquals(25, ratedPuzzleItem.ratingChange)
        assertEquals(2100000L, ratedPuzzleItem.timeSpent)

        val streakItem = result.history[5].data as User.HistoryItem.HistoryItemData.PuzzleStreakData
        assertEquals(12, streakItem.finalStreakCount)
        assertEquals(3600000L, streakItem.timeSpent)

        val failedItem = result.history[6].data as User.HistoryItem.HistoryItemData.FailedPuzzleData
        assertEquals(4, failedItem.puzzlesSolved)
        assertEquals(1000000L, failedItem.timeSpent)
    }

    @Test
    fun given_userWithAllAuthProviders_WHEN_saveAndGet_THEN_authenticationSerializedCorrectly() = runBlocking {
        val authProviders = listOf(
            User.AuthenticationState.AuthProvider.EMAIL,
            User.AuthenticationState.AuthProvider.GOOGLE,
            User.AuthenticationState.AuthProvider.INSTAGRAM,
            User.AuthenticationState.AuthProvider.APPLE,
            User.AuthenticationState.AuthProvider.FACEBOOK
        )

        for (provider in authProviders) {
            // Given
            val user = User(
                deviceId = TEST_DEVICE_ID,
                authentication = if (provider == User.AuthenticationState.AuthProvider.EMAIL) {
                    null
                } else {
                    User.AuthenticationState(
                        provider = provider,
                        userId = "user_${provider.name.lowercase()}_123"
                    )
                }
            )

            // When
            underTest.saveUser(user)
            val result = underTest.getUser()

            // Then
            assertEquals(user, result)
            if (provider == User.AuthenticationState.AuthProvider.EMAIL) {
                assertEquals(null, result.authentication)
            } else {
                assertEquals(provider, result.authentication?.provider)
                assertEquals("user_${provider.name.lowercase()}_123", result.authentication?.userId)
            }
        }
    }

    @Test
    fun given_savedUser_WHEN_updateUser_THEN_updatesCorrectly() = runBlocking {
        // Given
        val originalUser = User(
            deviceId = TEST_DEVICE_ID,
            profile = User.Profile(firstName = "Original", lastName = "User"),
            ratings = User.Ratings(current = 1200)
        )
        underTest.saveUser(originalUser)

        // When
        underTest.updateUser { user ->
            user.copy(
                profile = user.profile.copy(firstName = "Updated"),
                ratings = user.ratings.copy(current = 1500),
                failedPuzzles = listOf(1, 2, 3)
            )
        }

        // Then
        val result = underTest.getUser()
        assertEquals("Updated", result.profile.firstName)
        assertEquals("User", result.profile.lastName)
        assertEquals(1500, result.ratings.current)
        assertEquals(listOf(1, 2, 3), result.failedPuzzles)
    }

    @Test
    fun given_savedUser_WHEN_updateUserMultipleTimes_THEN_allUpdatesApplied() = runBlocking {
        // Given
        underTest.saveUser(User(deviceId = TEST_DEVICE_ID))

        // When
        underTest.updateUser { user ->
            user.copy(profile = user.profile.copy(firstName = "Step1"))
        }

        underTest.updateUser { user ->
            user.copy(profile = user.profile.copy(lastName = "Step2"))
        }

        underTest.updateUser { user ->
            user.copy(ratings = user.ratings.copy(current = 1800))
        }

        // Then
        val result = underTest.getUser()
        assertEquals("Step1", result.profile.firstName)
        assertEquals("Step2", result.profile.lastName)
        assertEquals(1800, result.ratings.current)
    }

    @Test
    fun given_savedUser_WHEN_userUpdatesFlow_THEN_emitsCurrentUser() = runBlocking {
        // Given
        val user = createComplexUser()
        underTest.saveUser(user)

        // When
        val result = underTest.userUpdates().first()

        // Then
        assertEquals(user, result)
    }

    @Test
    fun given_savedUser_WHEN_clearUserData_THEN_returnsToDefault() = runBlocking {
        // Given
        val user = createComplexUser()
        underTest.saveUser(user)
        assertEquals(user, underTest.getUser())

        // When
        underTest.clearUserData()

        // Then
        val result = underTest.getUser()
        assertTrue(result.deviceId.isNotEmpty())
        assertEquals(User(deviceId = result.deviceId), result)
    }

    @Test
    fun given_extremelyLargeData_WHEN_saveAndGet_THEN_handlesLargeDataCorrectly() = runBlocking {
        // Given
        val largeHistoryList = (1..100).map { index ->
            User.HistoryItem(
                timestamp = LocalDate.of(2024, 1, 1).plusDays(index.toLong()),
                data = when (index % 7) {
                    0 -> User.HistoryItem.HistoryItemData.PuzzleRushData(
                        tries = index,
                        bestScore = index * 10,
                        timeSpent = index * 60000L
                    )

                    1 -> User.HistoryItem.HistoryItemData.BoardVisualizationData(
                        sessionsCompleted = index,
                        timeSpent = index * 30000L
                    )

                    2 -> User.HistoryItem.HistoryItemData.BlindModeTrainingData(
                        tries = index,
                        mostMovesCompleted = index + 5,
                        timeSpent = index * 45000L
                    )

                    3 -> User.HistoryItem.HistoryItemData.BlindModeData(
                        played = index,
                        ratingChange = if (index % 2 == 0) index else -index,
                        timeSpent = index * 40000L
                    )

                    4 -> User.HistoryItem.HistoryItemData.RatedPuzzleData(
                        puzzlesPlayed = index,
                        puzzlesSolved = index - 2,
                        ratingChange = index * 2,
                        timeSpent = index * 50000L
                    )

                    5 -> User.HistoryItem.HistoryItemData.PuzzleStreakData(
                        finalStreakCount = index,
                        timeSpent = index * 55000L
                    )

                    else -> User.HistoryItem.HistoryItemData.FailedPuzzleData(
                        puzzlesSolved = index,
                        timeSpent = index * 35000L
                    )
                }
            )
        }

        val largeFailedPuzzlesList = (1..500).toList()

        val userWithLargeData = User(
            deviceId = TEST_DEVICE_ID,
            profile = User.Profile(
                firstName = "Large",
                lastName = "Data",
                joinDate = LocalDate.of(2020, 1, 1)
            ),
            history = largeHistoryList,
            failedPuzzles = largeFailedPuzzlesList
        )

        // When
        underTest.saveUser(userWithLargeData)
        val result = underTest.getUser()

        // Then
        assertEquals(userWithLargeData, result)
        assertEquals(100, result.history.size)
        assertEquals(500, result.failedPuzzles.size)

        assertEquals(LocalDate.of(2024, 1, 2), result.history.first().timestamp)
        assertEquals(LocalDate.of(2024, 4, 10), result.history.last().timestamp)
        assertEquals(1, result.failedPuzzles.first())
        assertEquals(500, result.failedPuzzles.last())
    }

    @Test
    fun given_userWithEdgeCaseDates_WHEN_saveAndGet_THEN_datesSerializedCorrectly() = runBlocking {
        val edgeCaseDates = listOf(
            LocalDate.of(1970, 1, 1),
            LocalDate.of(2000, 2, 29),
            LocalDate.of(2024, 12, 31),
            LocalDate.now(),
            LocalDate.now().minusYears(50),
            LocalDate.now().plusYears(50)
        )

        for (date in edgeCaseDates) {
            // Given
            val user = User(
                deviceId = TEST_DEVICE_ID,
                profile = User.Profile(joinDate = date),
                history = listOf(
                    User.HistoryItem(
                        timestamp = date,
                        data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                            tries = 1,
                            bestScore = 50,
                            timeSpent = 300000L
                        )
                    )
                )
            )

            // When
            underTest.saveUser(user)
            val result = underTest.getUser()

            // Then
            assertEquals(user, result)
            assertEquals(date, result.profile.joinDate)
            assertEquals(date, result.history.first().timestamp)
        }
    }

    private fun createComplexUser(): User = User(
        deviceId = TEST_DEVICE_ID,
        profile = User.Profile(
            firstName = "Alice",
            lastName = "Smith",
            joinDate = LocalDate.of(2022, 3, 10),
            avatarUrl = "https://example.com/alice.jpg"
        ),
        ratings = User.Ratings(
            current = 1650,
            blindMode = 800
        ),
        highScores = User.HighScores(
            ratedPuzzle = 1650,
            puzzleRush = 120,
            blindMode = 800
        ),
        statistics = User.Statistics(
            puzzlesPlayed = 250,
            puzzlesSolved = 200,
            totalTimeSpent = 7200000L,
        ),
        history = listOf(
            User.HistoryItem(
                timestamp = LocalDate.now().minusDays(1),
                data = User.HistoryItem.HistoryItemData.PuzzleRushData(
                    tries = 3,
                    bestScore = 120,
                    timeSpent = 900000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.now().minusDays(2),
                data = User.HistoryItem.HistoryItemData.RatedPuzzleData(
                    puzzlesPlayed = 10,
                    puzzlesSolved = 8,
                    ratingChange = 35,
                    timeSpent = 1500000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.now().minusDays(3),
                data = User.HistoryItem.HistoryItemData.BlindModeData(
                    played = 2,
                    ratingChange = -10,
                    timeSpent = 800000L
                )
            ),
            User.HistoryItem(
                timestamp = LocalDate.now().minusDays(4),
                data = User.HistoryItem.HistoryItemData.BoardVisualizationData(
                    sessionsCompleted = 5,
                    timeSpent = 1200000L
                )
            )
        ),
        failedPuzzles = listOf(42, 137, 289),
        authentication = User.AuthenticationState(
            provider = User.AuthenticationState.AuthProvider.GOOGLE,
            userId = "user_alice_123"
        )
    )
}
