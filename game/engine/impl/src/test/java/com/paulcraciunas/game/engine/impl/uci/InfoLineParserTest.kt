package com.paulcraciunas.game.engine.impl.uci

import com.paulcraciunas.game.engine.api.Evaluation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class InfoLineParserTest {

    @Test
    fun `GIVEN valid cp info line WHEN parse THEN returns parsed data`() {
        val line = "info depth 15 seldepth 20 multipv 1 score cp 30 nodes 933 nps 233250 time 4 pv e2e4 e7e5 g1f3"

        val result = InfoLineParser.parse(line)

        assertEquals(15, result?.depth)
        assertEquals(1, result?.multiPv)
        assertEquals(Evaluation.Centipawns(30), result?.evaluation)
        assertEquals(listOf("e2e4", "e7e5", "g1f3"), result?.principalVariation)
    }

    @Test
    fun `GIVEN mate score WHEN parse THEN returns Mate evaluation`() {
        val line = "info depth 20 seldepth 18 multipv 1 score mate 5 nodes 1000 nps 500000 time 2 pv d1h5 f7f6 d1f7"

        val result = InfoLineParser.parse(line)

        assertEquals(Evaluation.Mate(5), result?.evaluation)
    }

    @Test
    fun `GIVEN negative mate score WHEN parse THEN returns negative Mate`() {
        val line = "info depth 20 seldepth 18 multipv 2 score mate -3 nodes 1000 nps 500000 time 2 pv e7e5"

        val result = InfoLineParser.parse(line)

        assertEquals(Evaluation.Mate(-3), result?.evaluation)
    }

    @Test
    fun `GIVEN negative cp score WHEN parse THEN returns negative centipawns`() {
        val line = "info depth 10 seldepth 15 multipv 1 score cp -45 nodes 500 nps 250000 time 2 pv d7d5"

        val result = InfoLineParser.parse(line)

        assertEquals(Evaluation.Centipawns(-45), result?.evaluation)
    }

    @Test
    fun `GIVEN multipv 3 WHEN parse THEN returns correct multiPv`() {
        val line = "info depth 12 seldepth 16 multipv 3 score cp 12 nodes 800 nps 400000 time 2 pv b1c3 e7e5"

        val result = InfoLineParser.parse(line)

        assertEquals(3, result?.multiPv)
        assertEquals(listOf("b1c3", "e7e5"), result?.principalVariation)
    }

    @Test
    fun `GIVEN info string line WHEN parse THEN returns null`() {
        val line = "info string NNUE evaluation using nn-ad9b42354671.nnue enabled"

        assertNull(InfoLineParser.parse(line))
    }

    @Test
    fun `GIVEN non-info line WHEN parse THEN returns null`() {
        assertNull(InfoLineParser.parse("bestmove e2e4"))
        assertNull(InfoLineParser.parse("readyok"))
        assertNull(InfoLineParser.parse("uciok"))
    }

    @Test
    fun `GIVEN info line missing depth WHEN parse THEN returns null`() {
        val line = "info seldepth 20 multipv 1 score cp 30 nodes 933 nps 233250 time 4 pv e2e4"

        assertNull(InfoLineParser.parse(line))
    }

    @Test
    fun `GIVEN info line missing multipv WHEN parse THEN returns null`() {
        val line = "info depth 15 seldepth 20 score cp 30 nodes 933 nps 233250 time 4 pv e2e4"

        assertNull(InfoLineParser.parse(line))
    }

    @Test
    fun `GIVEN info line missing pv WHEN parse THEN returns null`() {
        val line = "info depth 15 seldepth 20 multipv 1 score cp 30 nodes 933 nps 233250 time 4"

        assertNull(InfoLineParser.parse(line))
    }

    @Test
    fun `GIVEN info line missing score WHEN parse THEN returns null`() {
        val line = "info depth 15 seldepth 20 multipv 1 nodes 933 nps 233250 time 4 pv e2e4"

        assertNull(InfoLineParser.parse(line))
    }

    @Test
    fun `GIVEN single move pv WHEN parse THEN returns single element list`() {
        val line = "info depth 1 seldepth 1 multipv 1 score cp 18 nodes 20 nps 10000 time 2 pv e2e4"

        val result = InfoLineParser.parse(line)

        assertEquals(listOf("e2e4"), result?.principalVariation)
    }

    @Test
    fun `GIVEN info line with upperbound WHEN parse THEN still parses score`() {
        val line = "info depth 10 seldepth 12 multipv 1 score cp 25 upperbound nodes 500 nps 250000 time 2 pv e2e4"

        val result = InfoLineParser.parse(line)

        assertEquals(Evaluation.Centipawns(25), result?.evaluation)
    }

    @Test
    fun `GIVEN info line with extra whitespace WHEN parse THEN handles correctly`() {
        val line = "info  depth  15  seldepth 20  multipv  1  score  cp  30  pv  e2e4 e7e5"
        val result = InfoLineParser.parse(line)
        assertEquals(15, result?.depth)
        assertEquals(1, result?.multiPv)
        assertEquals(Evaluation.Centipawns(30), result?.evaluation)
        assertEquals(listOf("e2e4", "e7e5"), result?.principalVariation)
    }
}
