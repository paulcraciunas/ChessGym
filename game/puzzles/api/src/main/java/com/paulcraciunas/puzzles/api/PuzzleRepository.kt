package com.paulcraciunas.puzzles.api

import com.paulcraciunas.game.logic.api.Puzzle

interface PuzzleRepository {
    suspend fun get(count: Int): List<Puzzle>
    suspend fun getById(id: Int): Puzzle?
    suspend fun getByRating(targetRating: Int): Puzzle?
    suspend fun getByRatingRange(min: Int, max: Int): Puzzle?
}
