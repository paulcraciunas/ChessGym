package com.paulcraciunas.data.api

import com.paulcraciunas.data.api.PuzzleRepository.Defaults.INCREMENT
import com.paulcraciunas.data.db.puzzle.PuzzleWithThemes
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject

// TODO Paul: Create domain gradle module
// TODO Paul: Move to domain module
// TODO Paul: We also need to integrate the serialization into this. Write the puzzles in binary, so they take up less space
interface PuzzleRepository {
    suspend fun insertFromList(puzzleData: List<String>)
    suspend fun getRandomPuzzleByRating(targetRating: Int): PuzzleWithThemes?
    suspend fun getRandomPuzzles(count: Int): List<PuzzleWithThemes>
    suspend fun getRandomPuzzlesByTheme(theme: String, count: Int): List<PuzzleWithThemes>
    suspend fun getByIncreasingRatings(
        count: Int = COUNT,
        increment: Int = INCREMENT,
        from: Int = RATING_START
    ): List<PuzzleWithThemes>

    companion object Defaults {
        const val RATING_START = 400
        const val INCREMENT = 40
        const val COUNT = 100
    }
}

class PuzzleRepositoryImpl @Inject constructor(private val db: PuzzleDatabase) : PuzzleRepository {
    override suspend fun insertFromList(puzzleData: List<String>) =
        db.insertFromList(puzzleData)

    override suspend fun getRandomPuzzleByRating(targetRating: Int): PuzzleWithThemes? =
        db.puzzleDao().getRandomPuzzleByRating(targetRating)

    override suspend fun getRandomPuzzles(count: Int): List<PuzzleWithThemes> =
        db.puzzleDao().getRandomPuzzles(count)

    override suspend fun getRandomPuzzlesByTheme(
        theme: String,
        count: Int
    ): List<PuzzleWithThemes> = db.puzzleDao().getRandomPuzzlesByTheme(theme, count)

    // TODO Paul: test me
    override suspend fun getByIncreasingRatings(
        count: Int,
        increment: Int,
        from: Int
    ): List<PuzzleWithThemes> {
        val result = mutableListOf<PuzzleWithThemes>()
        var rating = from

        repeat(count) {
            db.puzzleDao().getRandomPuzzleByRating(rating)?.let {
                result.add(it)
                rating += ThreadLocalRandom.current().nextInt(1, INCREMENT)
            } ?: return@repeat
        }
        return result
    }
}