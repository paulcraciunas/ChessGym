package com.paulcraciunas.game.logic.board

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.api.board.loc
import com.paulcraciunas.game.logic.assertDefaultBoard
import com.paulcraciunas.game.logic.impl.board.BoardFactory
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

        other.remove("a2".loc())

        assertTrue(underTest.has(Piece.Pawn, Side.WHITE, Locus(File.a, Rank.`2`)))
    }
}
