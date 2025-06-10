package com.paulcraciunas.serializer.api

import com.paulcraciunas.game.logic.api.IPuzzle

interface PuzzleReader {
    fun read(bytes: ByteArray): String
    fun readPuzzle(bytes: ByteArray): IPuzzle
}
