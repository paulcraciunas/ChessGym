package com.paulcraciunas.game.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class PgnSerializerTest {

    private val underTest = PgnSerializer

    @ParameterizedTest(name = "Loading game {0} from pgn")
    @MethodSource("pgnGames")
    fun `WHEN loading a valid game THEN game loads`(
        pgnFile: String,
    ) {
        underTest.from(ClassLoader.getSystemResource(pgnFile).readText())
    }

    @Test
    fun `WHEN serializing a loaded game THEN contents are identical`() {
        val gameString = ClassLoader.getSystemResource("Kasparov_Karpov_WC_1986_round_1.pgn")
            .readText()

        assertEquals(gameString, underTest.of(underTest.from(gameString)))
    }

    companion object {
        @JvmStatic
        fun pgnGames(): List<Arguments> =
            listOf<Arguments>(
                // TODO Paul: add more games
                Arguments.of("AnandKramnik2007.pgn"),
                Arguments.of("FischerSpasskyRound6.pgn"),
                Arguments.of("GameOfCentury.pgn"),
                Arguments.of("Kasparov_Karpov_WC_1986_round_1.pgn"),
            )
    }
}
