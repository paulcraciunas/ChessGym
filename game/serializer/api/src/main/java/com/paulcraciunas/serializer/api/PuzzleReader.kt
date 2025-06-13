package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.Puzzle

interface PuzzleReader {
    fun read(bytes: ByteArray): String
    fun readPuzzle(rating: Int, bytes: ByteArray): Puzzle
}
