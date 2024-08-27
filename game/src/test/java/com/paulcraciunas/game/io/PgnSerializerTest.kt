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
                Arguments.of("AnandKramnik2007.pgn"),
                Arguments.of("FischerSpasskyRound6.pgn"),
                Arguments.of("GameOfCentury.pgn"),
                Arguments.of("Kasparov_Karpov_WC_1986_round_1.pgn"),
                Arguments.of("Kasparov_Karpov_WC_1986_round_1.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round1.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round2.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round3.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round4.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round5.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round6.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round7.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round8.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round9.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round10.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round11.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round12.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round13.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round14.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round15.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round16.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round17.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round18.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round19.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round20.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round21.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round22.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round23.pgn"),
                Arguments.of("Kasparov_Karpov_87WC_Round24.pgn"),
            )
    }
}
