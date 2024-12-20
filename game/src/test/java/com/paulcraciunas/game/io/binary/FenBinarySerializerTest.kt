package com.paulcraciunas.game.io.binary

import com.paulcraciunas.game.io.FenBinarySerializer
import com.paulcraciunas.game.io.FenSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class FenBinarySerializerTest {
    private val adapter = BinaryAdapter()

    private val underTest = FenBinarySerializer(
        BinaryPuzzleReader(FenSerializer, adapter),
        BinaryPuzzleWriter(FenSerializer, adapter)
    )

    @ParameterizedTest(name = "Loading puzzle {0}")
    @MethodSource("fenPuzzles")
    fun `WHEN serializing a loaded game THEN contents are identical`(fenGame: String) {
        val result = underTest.fromBinary(underTest.toBinary(fenGame))
        assertEquals(fenGame, result)
    }

    companion object {
        @JvmStatic
        fun fenPuzzles(): List<String> =
            ClassLoader.getSystemResource("fen_puzzles.csv").readText().split("\n")
    }
}
