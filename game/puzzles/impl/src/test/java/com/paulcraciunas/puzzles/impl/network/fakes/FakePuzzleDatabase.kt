package com.paulcraciunas.puzzles.impl.network.fakes

import com.paulcraciunas.puzzles.impl.db.Puzzle
import com.paulcraciunas.puzzles.impl.impl.PuzzleDatabase

internal class FakePuzzleDatabase : PuzzleDatabase {
    private var insertedId: Long = 0L

    val insertedPuzzles = mutableListOf<Puzzle>()
    var shouldThrowError = false

    override suspend fun insert(puzzle: Puzzle): Long {
        checkError()
        insertedPuzzles.add(puzzle)
        return ++insertedId
    }

    override suspend fun bulkInsert(all: List<Puzzle>) = checkError().also { insertedPuzzles.addAll(all) }
    override suspend fun get(count: Int): List<Puzzle> = checkError().run { insertedPuzzles.subList(0, count) }
    override suspend fun getById(id: Int): Puzzle? = checkError().run { insertedPuzzles.firstOrNull { it.id == id } }
    override suspend fun getByRating(rating: Int): Puzzle? = checkError().run { insertedPuzzles.firstOrNull { it.rating == rating } }

    override suspend fun getInRatingRange(min: Int, max: Int): Puzzle? =
        checkError().run { insertedPuzzles.firstOrNull { it.rating in min..max } }

    private fun checkError() {
        if (shouldThrowError) {
            throw RuntimeException("Database error")
        }
    }
}
