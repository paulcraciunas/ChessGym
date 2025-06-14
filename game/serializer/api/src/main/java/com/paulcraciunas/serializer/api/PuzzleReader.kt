package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.Puzzle

interface PuzzleReader {
    fun readPuzzle(rating: Int, bytes: ByteArray): Puzzle
}
