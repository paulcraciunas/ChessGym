package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.domain.api.puzzles.GetStreakPuzzle
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [GetStreakPuzzle] that fetches puzzles with increasing difficulty
 * based on the current streak count.
 *
 * If there's an active streak with a saved puzzle ID, that puzzle is loaded so the user
 * can continue where they left off. Otherwise, a new puzzle is fetched based on the
 * calculated rating: BASE_RATING + (currentCount * RATING_INCREMENT)
 *
 * When a new puzzle is fetched, the lastPuzzleId is updated so the user can continue later.
 */
class GetStreakPuzzleImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val puzzleRepository: PuzzleRepository,
    private val getPuzzleByRating: GetPuzzleByRating,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) : GetStreakPuzzle {

    override suspend fun invoke(): GetStreakPuzzle.Data = withContext(dispatcher) {
        val user = userRepository.get()
        val currentCount = user.ratings.puzzleStreak.currentCount
        val lastPuzzleId = user.ratings.puzzleStreak.lastPuzzleId

        // If we have a saved puzzle ID, load that puzzle to continue the streak
        if (lastPuzzleId != null) {
            val puzzle = puzzleRepository.getById(lastPuzzleId)
            if (puzzle != null) {
                return@withContext GetStreakPuzzle.Data(
                    puzzle = puzzle,
                    currentStreakCount = currentCount,
                )
            }
            // If puzzle not found (e.g., database cleared), fall through to fetch new one
        }

        // Calculate target rating based on current streak count
        val targetRating = GetStreakPuzzle.BASE_RATING + (currentCount * GetStreakPuzzle.RATING_INCREMENT)
        val puzzle = getPuzzleByRating(targetRating)

        // Save the puzzle ID so user can continue later
        val updatedUser = user.copy(
            ratings = user.ratings.copy(
                puzzleStreak = user.ratings.puzzleStreak.copy(lastPuzzleId = puzzle.id)
            )
        )
        userRepository.update(updatedUser)

        return@withContext GetStreakPuzzle.Data(
            puzzle = puzzle,
            currentStreakCount = currentCount,
        )
    }
}
