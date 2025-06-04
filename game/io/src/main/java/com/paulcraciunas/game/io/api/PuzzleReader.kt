package com.paulcraciunas.game.io.api

import com.paulcraciunas.game.logic.Puzzle

interface PuzzleReader {
    fun read(bytes: ByteArray): String
    fun readPuzzle(bytes: ByteArray): Puzzle
}
