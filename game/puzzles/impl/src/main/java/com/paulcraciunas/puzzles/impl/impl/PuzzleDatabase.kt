package com.paulcraciunas.puzzles.impl.impl

import com.paulcraciunas.puzzles.impl.db.Puzzle

interface PuzzleDatabase {
    suspend fun insert(puzzle: Puzzle): Long

    suspend fun bulkInsert(all: List<Puzzle>)

    suspend fun get(count: Int): List<Puzzle>

    suspend fun getByRating(rating: Int): Puzzle?

    suspend fun getInRatingRange(min: Int, max: Int): Puzzle?
}
