package com.paulcraciunas.puzzles.api

import com.paulcraciunas.game.logic.api.IPuzzle

interface PuzzleRepository {
    suspend fun get(count: Int): List<IPuzzle>
    suspend fun getByRating(targetRating: Int): IPuzzle?
    suspend fun getByRatingRange(min: Int, max: Int): IPuzzle?
}
