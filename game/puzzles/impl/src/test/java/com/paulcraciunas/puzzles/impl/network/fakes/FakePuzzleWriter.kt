package com.paulcraciunas.puzzles.impl.network.fakes

import com.paulcraciunas.serializer.api.PuzzleWriter

internal class FakePuzzleWriter : PuzzleWriter {
    override fun write(puzzleAndMoves: String): ByteArray = puzzleAndMoves.toByteArray()
    override fun write(puzzle: String, moves: String): ByteArray = "$puzzle|$moves".toByteArray()
}
