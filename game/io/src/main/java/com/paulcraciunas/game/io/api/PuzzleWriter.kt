package com.paulcraciunas.game.io.api

interface PuzzleWriter {
    /**
     * Expected format is "board,moves"; e.g. "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1,e2e4 d7d5"
     */
    fun write(puzzleAndMoves: String): ByteArray

    fun write(puzzle: String, moves: String): ByteArray
}
