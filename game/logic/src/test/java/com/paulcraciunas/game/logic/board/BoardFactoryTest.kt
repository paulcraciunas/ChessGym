package com.paulcraciunas.game.logic.board

import com.paulcraciunas.game.logic.Side.WHITE
import com.paulcraciunas.game.logic.assertDefaultBoard
import com.paulcraciunas.game.logic.board.File.a
import com.paulcraciunas.game.logic.board.Rank.`2`
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BoardFactoryTest {

    private val underTest = BoardFactory.defaultBoard()

    @Test
    fun `WHEN creating a default board THEN it has all expected starting pieces`() {
        assertDefaultBoard(underTest)
    }

    @Test
    fun `WHEN creating multiple default boards THEN they have their own sets of pieces`() {
        val other = BoardFactory.defaultBoard()

        other.remove(Locus(a, `2`))

        assertTrue(underTest.has(Piece.Pawn, WHITE, Locus(a, `2`)))
    }
}
