package com.paulcraciunas.chessgym.dsl.setup

import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class UserSetup(private val repository: FakeUserRepository) {

    fun isDefault(): UserSetup = apply {
        runBlocking { repository.update(User()) }
    }

    fun isEmpty(): UserSetup = apply {
        runBlocking { repository.clear() }
    }

    fun isLoaded(
        firstName: String = "Chess",
        lastName: String = "Enthusiast",
        rating: Int = 1200,
        joinDate: LocalDate = LocalDate.now(),
    ): UserSetup = apply {
        val user = User(
            profile = User.Profile(
                firstName = firstName,
                lastName = lastName,
                joinDate = joinDate,
            ),
            ratings = User.Ratings(current = rating),
        )
        runBlocking { repository.update(user) }
    }

    fun withRating(rating: Int): UserSetup = apply {
        runBlocking {
            val current = repository.get()
            repository.update(
                current.copy(ratings = current.ratings.copy(current = rating))
            )
        }
    }

    fun withHighScores(
        ratedPuzzle: Int = 1200,
        puzzleRush: Int = 0,
        puzzleStreak: Int = 0,
        findTheSquare: Int = 0,
        moveThePiece: Int = 0,
        blindMode: Int = 400,
    ): UserSetup = apply {
        runBlocking {
            val current = repository.get()
            repository.update(
                current.copy(
                    highScores = User.HighScores(
                        ratedPuzzle = ratedPuzzle,
                        puzzleRush = puzzleRush,
                        puzzleStreak = puzzleStreak,
                        findTheSquare = findTheSquare,
                        moveThePiece = moveThePiece,
                        blindMode = blindMode,
                    )
                )
            )
        }
    }

    fun withStatistics(
        puzzlesPlayed: Int = 0,
        puzzlesSolved: Int = 0,
        totalTimeSpent: Long = 0,
    ): UserSetup = apply {
        runBlocking {
            val current = repository.get()
            repository.update(
                current.copy(
                    statistics = User.Statistics(
                        puzzlesPlayed = puzzlesPlayed,
                        puzzlesSolved = puzzlesSolved,
                        totalTimeSpent = totalTimeSpent,
                    )
                )
            )
        }
    }

    fun withPuzzleStreak(currentCount: Int, lastPuzzleId: Int? = null): UserSetup = apply {
        runBlocking {
            val current = repository.get()
            repository.update(
                current.copy(
                    ratings = current.ratings.copy(
                        puzzleStreak = User.PuzzleStreak(
                            currentCount = currentCount,
                            lastPuzzleId = lastPuzzleId,
                        )
                    )
                )
            )
        }
    }

    fun withFailedPuzzles(vararg puzzleIds: Int): UserSetup = apply {
        runBlocking {
            val current = repository.get()
            repository.update(current.copy(failedPuzzles = puzzleIds.toList()))
        }
    }

    fun withHistory(vararg items: User.HistoryItem): UserSetup = apply {
        runBlocking {
            val current = repository.get()
            repository.update(current.copy(history = items.toList()))
        }
    }
}
