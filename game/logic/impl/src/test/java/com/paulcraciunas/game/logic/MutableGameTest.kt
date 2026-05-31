package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.MutableGame
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.game.logic.impl.board.BoardFactory
import com.paulcraciunas.game.logic.impl.plies.StandardPly
import com.paulcraciunas.game.logic.plies.ExpectedPly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MutableGameTest {
    private val on = BoardFactory.defaultBoard()

    private lateinit var underTest: MutableGame

    @Nested
    internal inner class PliesAndPlay {
        @Test
        fun `GIVEN default starting board WHEN getting plies for white THEN return all correct plies`() {
            underTest = MutableGame(board = on).apply { start() }
            val expected = mutableListOf<StandardPly>()
                .apply {
                    add(StandardPly(Side.WHITE, Piece.Pawn, Locus.e2, Locus.e3))
                    add(StandardPly(Side.WHITE, Piece.Pawn, Locus.e2, Locus.e4))
                }.map { ExpectedPly(it) }

            val actual = underTest.plies(Locus.e2)
                .map { ExpectedPly(it) }

            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.InProgress, underTest.state)
            assertEquals(expected.size, actual.size)
            assertTrue(expected.containsAll(actual))
            assertTrue(actual.containsAll(expected))
        }

        @Test
        fun `GIVEN default starting board WHEN getting plies for black THEN return all correct plies`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play(
                underTest.plies(Locus.e2).first { it.to == Locus.e4 }
            )
            val expected = mutableListOf<StandardPly>()
                .apply {
                    add(StandardPly(Side.BLACK, Piece.Pawn, Locus.e7, Locus.e6))
                    add(StandardPly(Side.BLACK, Piece.Pawn, Locus.e7, Locus.e5))
                }.map { ExpectedPly(it) }

            val actual = underTest.plies(Locus.e7)
                .map { ExpectedPly(it) }

            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Game.GameState.InProgress, underTest.state)
            assertEquals(expected.size, actual.size)
            assertTrue(expected.containsAll(actual))
            assertTrue(actual.containsAll(expected))
        }
    }

    @Nested
    internal inner class GameOver {
        @Test
        fun `WHEN playing a Fools Mate game THEN game ends in checkmate`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.f2, Locus.f3)
                .playAnd(Locus.e7, Locus.e6)
                .playAnd(Locus.g2, Locus.g4)
                .playAnd(Locus.d8, Locus.h4)

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.CheckMate), underTest.state)
        }

        @Test
        fun `WHEN playing a Scholars Mate game THEN game ends in checkmate`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.d1, Locus.h5)
                .playAnd(Locus.b8, Locus.c6)
                .playAnd(Locus.f1, Locus.c4)
                .playAnd(Locus.g8, Locus.f6)
                .playAnd(Locus.h5, Locus.f7)

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.CheckMate), underTest.state)
        }

        @Test
        fun `WHEN repeating position 3 times THEN game ends in draw`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest
                .playAnd(Locus.e2, Locus.e4).playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.e1, Locus.e2).playAnd(Locus.e8, Locus.e7)
                .playAnd(Locus.e2, Locus.e1).playAnd(Locus.e7, Locus.e8)
                .playAnd(Locus.g1, Locus.f3).playAnd(Locus.b8, Locus.c6)
                .playAnd(Locus.f3, Locus.g1).playAnd(Locus.c6, Locus.b8)

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.DrawByRepetition), underTest.state)
        }

        @Test
        fun `WHEN loading a stalemate position for black THEN game is over`() {
            val board = Board().apply {
                add(Piece.King, Side.WHITE, Locus.a1)
                add(Piece.Queen, Side.WHITE, Locus.g6)
                add(Piece.King, Side.BLACK, Locus.h8)
            }
            underTest = MutableGame(board = board, turn = Side.BLACK).apply { start() }

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.StaleMate), underTest.state)
        }

        @Test
        fun `WHEN loading a stalemate position for white THEN game is over`() {
            val board = Board().apply {
                add(Piece.King, Side.WHITE, Locus.f1)
                add(Piece.Pawn, Side.BLACK, Locus.f2)
                add(Piece.King, Side.BLACK, Locus.f3)
            }
            underTest = MutableGame(board = board).apply { start() }

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.StaleMate), underTest.state)
        }

        @Test
        fun `WHEN loading a position with just 2 kings THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, Locus.f1),
                Triple(Piece.King, Side.BLACK, Locus.f3),
            )
        }

        @Test
        fun `WHEN loading a position with an extra Bishop THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, Locus.f1),
                Triple(Piece.Bishop, Side.WHITE, Locus.a7),
                Triple(Piece.King, Side.BLACK, Locus.f3),
            )
        }

        @Test
        fun `WHEN loading a position with an extra Knight THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, Locus.f1),
                Triple(Piece.Knight, Side.WHITE, Locus.a7),
                Triple(Piece.King, Side.BLACK, Locus.f3),
            )
        }

        @Test
        fun `WHEN loading a position with a same colour Bishop each THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, Locus.f1),
                Triple(Piece.Bishop, Side.WHITE, Locus.a7),
                Triple(Piece.Bishop, Side.BLACK, Locus.h4),
                Triple(Piece.King, Side.BLACK, Locus.f3),
            )
        }

        private fun verifyDrawByInsufficientMaterial(vararg pieces: Triple<Piece, Side, Locus>) {
            val board = Board()
            pieces.forEach { board.add(it.first, it.second, it.third) }
            underTest = MutableGame(board = board).apply { start() }

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.DrawByInsufficientMaterial), underTest.state)
        }
    }

    @Nested
    internal inner class UndoLast {
        @Test
        fun `GIVEN fresh game WHEN canUndo THEN returns false`() {
            underTest = MutableGame(board = on).apply { start() }

            assertFalse(underTest.canUndo())
        }

        @Test
        fun `GIVEN game with 1 move WHEN undoLast THEN board reverts AND turn reverts`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
            assertEquals(Side.BLACK, underTest.info.turn)

            underTest.undoLast()

            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Piece.Pawn, underTest.board.at(Locus.e2))
            assertTrue(underTest.board.isEmpty(Locus.e4))
        }

        @Test
        fun `GIVEN game with checkmate WHEN undoLast THEN state becomes InProgress AND plies available`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.f2, Locus.f3)
                .playAnd(Locus.e7, Locus.e6)
                .playAnd(Locus.g2, Locus.g4)
                .playAnd(Locus.d8, Locus.h4)
            assertEquals(Game.GameState.Finished(Result.CheckMate), underTest.state)

            underTest.undoLast()

            assertEquals(Game.GameState.InProgress, underTest.state)
            assertEquals(Side.BLACK, underTest.info.turn)
            assertTrue(underTest.plies().isNotEmpty())
        }

        @Test
        fun `GIVEN game with castling move WHEN undoLast THEN king AND rook return`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)
                .playAnd(Locus.b8, Locus.c6)
                .playAnd(Locus.f1, Locus.e2)
                .playAnd(Locus.d7, Locus.d6)
                .playAnd(Locus.e1, Locus.g1) // kingside castle

            assertEquals(Piece.King, underTest.board.at(Locus.g1))
            assertEquals(Piece.Rook, underTest.board.at(Locus.f1))

            underTest.undoLast()

            assertEquals(Piece.King, underTest.board.at(Locus.e1))
            assertEquals(Piece.Rook, underTest.board.at(Locus.h1))
            assertTrue(underTest.board.isEmpty(Locus.g1))
            assertTrue(underTest.board.isEmpty(Locus.f1))
        }

        @Test
        fun `GIVEN game with en passant WHEN undoLast THEN captured pawn returns`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.a7, Locus.a6)
                .playAnd(Locus.e4, Locus.e5)
                .playAnd(Locus.d7, Locus.d5) // pawn moves 2 squares, enabling en passant
                .playAnd(Locus.e5, Locus.d6) // en passant capture

            assertTrue(underTest.board.isEmpty(Locus.d5))
            assertEquals(Piece.Pawn, underTest.board.at(Locus.d6))

            underTest.undoLast()

            assertEquals(Piece.Pawn, underTest.board.at(Locus.e5))
            assertEquals(Piece.Pawn, underTest.board.at(Locus.d5))
            assertTrue(underTest.board.isEmpty(Locus.d6))
        }

        @Test
        fun `GIVEN game with promotion WHEN undoLast THEN pawn returns AND promoted piece removed`() {
            val board = Board().apply {
                add(Piece.King, Side.WHITE, Locus.e1)
                add(Piece.King, Side.BLACK, Locus.e8)
                add(Piece.Pawn, Side.WHITE, Locus.a7)
            }
            underTest = MutableGame(board = board).apply { start() }
            val promotionPly = underTest.plies(Locus.a7).first { it.to == Locus.a8 }
            promotionPly.promote(Piece.Queen)
            underTest.play(promotionPly)

            assertEquals(Piece.Queen, underTest.board.at(Locus.a8))

            underTest.undoLast()

            assertEquals(Piece.Pawn, underTest.board.at(Locus.a7))
            assertTrue(underTest.board.isEmpty(Locus.a8))
        }

        @Test
        fun `GIVEN game after undoLast WHEN play different move THEN canReplay is false`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)

            underTest.undoLast()
            assertTrue(underTest.canReplay())

            underTest.playAnd(Locus.d2, Locus.d4)

            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game WHEN undoLast THEN lastPly highlights reflect previous move`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)

            underTest.undoLast()

            val lastPly = underTest.info.lastPly
            assertNotNull(lastPly)
            assertEquals(Locus.e7, lastPly!!.from)
            assertEquals(Locus.e5, lastPly.to)
        }

        @Test
        fun `GIVEN game with 1 move WHEN undoLast THEN lastPly is null`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)

            underTest.undoLast()

            assertNull(underTest.info.lastPly)
        }
    }

    @Nested
    internal inner class UndoAll {
        @Test
        fun `GIVEN a game WHEN undoing all THEN board is back to start`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)

            underTest.undoAll()

            assertFalse(underTest.canUndo())
            assertTrue(underTest.canReplay())
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(0, underTest.history.size)
            assertEquals(Piece.Pawn, underTest.board.at(Locus.e2))
            assertTrue(underTest.board.isEmpty(Locus.e4))
        }

        @Test
        fun `GIVEN game with no moves WHEN undoAll THEN nothing changes`() {
            underTest = MutableGame(board = on).apply { start() }

            underTest.undoAll()

            assertFalse(underTest.canUndo())
            assertFalse(underTest.canReplay())
            assertEquals(Side.WHITE, underTest.info.turn)
        }

        @Test
        fun `GIVEN game ending in checkmate WHEN undoAll THEN game is in progress`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.f2, Locus.f3)
                .playAnd(Locus.e7, Locus.e6)
                .playAnd(Locus.g2, Locus.g4)
                .playAnd(Locus.d8, Locus.h4)
            assertEquals(Game.GameState.Finished(Result.CheckMate), underTest.state)

            underTest.undoAll()

            assertEquals(Game.GameState.InProgress, underTest.state)
            assertEquals(Side.WHITE, underTest.info.turn)
            assertNull(underTest.info.lastPly)
        }
    }

    @Nested
    internal inner class ReplayNext {
        @Test
        fun `GIVEN game at start WHEN canReplay THEN returns false`() {
            underTest = MutableGame(board = on).apply { start() }

            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game after undoLast WHEN replayNext THEN board advances AND turn changes`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
            underTest.undoLast()
            assertEquals(Side.WHITE, underTest.info.turn)

            underTest.replayNext()

            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Piece.Pawn, underTest.board.at(Locus.e4))
            assertTrue(underTest.board.isEmpty(Locus.e2))
        }

        @Test
        fun `GIVEN game at end WHEN canReplay THEN returns false`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)

            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game WHEN undo then replay THEN lastPly highlights are correct`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
            underTest.undoLast()

            underTest.replayNext()

            val lastPly = underTest.info.lastPly
            assertNotNull(lastPly)
            assertEquals(Locus.e7, lastPly!!.from)
            assertEquals(Locus.e5, lastPly.to)
        }
    }

    @Nested
    internal inner class ReplayAll {
        @Test
        fun `GIVEN game at start after undoAll WHEN replayAll THEN board matches end state`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)
            underTest.undoAll()

            underTest.replayAll()

            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Piece.Pawn, underTest.board.at(Locus.e4))
            assertEquals(Piece.Pawn, underTest.board.at(Locus.e5))
            assertEquals(Piece.Knight, underTest.board.at(Locus.f3))
            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game with no future WHEN replayAll THEN nothing changes`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
            val turnBefore = underTest.info.turn

            underTest.replayAll()

            assertEquals(turnBefore, underTest.info.turn)
            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game with multiple undos WHEN replayAll THEN history and board match original`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)
                .playAnd(Locus.b8, Locus.c6)

            val originalHistory = underTest.history.toList()
            underTest.undoAll()
            underTest.replayAll()

            assertEquals(originalHistory.size, underTest.history.size)
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Piece.Knight, underTest.board.at(Locus.f3))
            assertEquals(Piece.Knight, underTest.board.at(Locus.c6))
        }
    }

    @Nested
    internal inner class HistoryBranching {
        @Test
        fun `GIVEN a game WHEN navigating back and playing a different move THEN future history is discarded`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
            underTest.playAnd(Locus.e7, Locus.e5)

            assertTrue(underTest.canUndo())
            underTest.undoLast()
            underTest.undoLast()

            assertTrue(underTest.canReplay())
            underTest.playAnd(Locus.d2, Locus.d4)

            assertFalse(underTest.canReplay())
            assertEquals(1, underTest.history.size)
        }

        @Test
        fun `GIVEN game with 3 moves WHEN undo 2 and play new THEN historySize is 1`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)

            underTest.undoLast()
            underTest.undoLast()
            underTest.playAnd(Locus.d7, Locus.d5)

            assertEquals(2, underTest.currentMoveIndex)
            assertEquals(2, underTest.historySize)
        }
    }

    @Nested
    internal inner class HistoryProperties {
        @Test
        fun `GIVEN fresh game WHEN checking properties THEN historySize is 0 and currentMoveIndex is 0`() {
            underTest = MutableGame(board = on).apply { start() }

            assertEquals(0, underTest.historySize)
            assertEquals(0, underTest.currentMoveIndex)
        }

        @Test
        fun `GIVEN game with 3 moves WHEN checking properties THEN historySize is 3 and index is 3`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)

            assertEquals(3, underTest.historySize)
            assertEquals(3, underTest.currentMoveIndex)
        }

        @Test
        fun `GIVEN game undone by 1 WHEN checking currentMoveIndex THEN is historySize minus 1`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.playAnd(Locus.e2, Locus.e4)
                .playAnd(Locus.e7, Locus.e5)
                .playAnd(Locus.g1, Locus.f3)
            underTest.undoLast()

            assertEquals(3, underTest.historySize)
            assertEquals(2, underTest.currentMoveIndex)
        }
    }

    private fun MutableGame.playAnd(from: Locus, to: Locus): MutableGame = apply {
        play(
            plies(from).firstOrNull { it.to == to }
                ?: throw AssertionError("Wrong move")
        )
    }
}
