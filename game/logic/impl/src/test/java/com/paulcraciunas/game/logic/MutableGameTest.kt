package com.paulcraciunas.game.logic

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.loc
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
                    add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e3".loc()))
                    add(StandardPly(Side.WHITE, Piece.Pawn, "e2".loc(), "e4".loc()))
                }.map { ExpectedPly(it) }

            val actual = underTest.plies("e2".loc())
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
                underTest.plies("e2".loc()).first { it.to == "e4".loc() }
            )
            val expected = mutableListOf<StandardPly>()
                .apply {
                    add(StandardPly(Side.BLACK, Piece.Pawn, "e7".loc(), "e6".loc()))
                    add(StandardPly(Side.BLACK, Piece.Pawn, "e7".loc(), "e5".loc()))
                }.map { ExpectedPly(it) }

            val actual = underTest.plies("e7".loc())
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
            underTest.play("f2", "f3")
                .play("e7", "e6")
                .play("g2", "g4")
                .play("d8", "h4")

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.CheckMate), underTest.state)
        }

        @Test
        fun `WHEN playing a Scholars Mate game THEN game ends in checkmate`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("d1", "h5")
                .play("b8", "c6")
                .play("f1", "c4")
                .play("g8", "f6")
                .play("h5", "f7")

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.CheckMate), underTest.state)
        }

        @Test
        fun `WHEN repeating position 3 times THEN game ends in draw`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest
                .play("e2", "e4").play("e7", "e5")
                .play("e1", "e2").play("e8", "e7")
                .play("e2", "e1").play("e7", "e8")
                .play("g1", "f3").play("b8", "c6")
                .play("f3", "g1").play("c6", "b8")

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.DrawByRepetition), underTest.state)
        }

        @Test
        fun `WHEN loading a stalemate position for black THEN game is over`() {
            val board = Board().apply {
                add(Piece.King, Side.WHITE, "a1".loc())
                add(Piece.Queen, Side.WHITE, "g6".loc())
                add(Piece.King, Side.BLACK, "h8".loc())
            }
            underTest = MutableGame(board = board, turn = Side.BLACK).apply { start() }

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.StaleMate), underTest.state)
        }

        @Test
        fun `WHEN loading a stalemate position for white THEN game is over`() {
            val board = Board().apply {
                add(Piece.King, Side.WHITE, "f1".loc())
                add(Piece.Pawn, Side.BLACK, "f2".loc())
                add(Piece.King, Side.BLACK, "f3".loc())
            }
            underTest = MutableGame(board = board).apply { start() }

            Locus.all { assertTrue(underTest.plies(it).isEmpty()) }
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Game.GameState.Finished(Result.StaleMate), underTest.state)
        }

        @Test
        fun `WHEN loading a position with just 2 kings THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, "f1"),
                Triple(Piece.King, Side.BLACK, "f3"),
            )
        }

        @Test
        fun `WHEN loading a position with an extra Bishop THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, "f1"),
                Triple(Piece.Bishop, Side.WHITE, "a7"),
                Triple(Piece.King, Side.BLACK, "f3"),
            )
        }

        @Test
        fun `WHEN loading a position with an extra Knight THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, "f1"),
                Triple(Piece.Knight, Side.WHITE, "a7"),
                Triple(Piece.King, Side.BLACK, "f3"),
            )
        }

        @Test
        fun `WHEN loading a position with a same colour Bishop each THEN draw by insufficient material`() {
            verifyDrawByInsufficientMaterial(
                Triple(Piece.King, Side.WHITE, "f1"),
                Triple(Piece.Bishop, Side.WHITE, "a7"),
                Triple(Piece.Bishop, Side.BLACK, "h4"),
                Triple(Piece.King, Side.BLACK, "f3"),
            )
        }

        private fun verifyDrawByInsufficientMaterial(vararg pieces: Triple<Piece, Side, String>) {
            val board = Board()
            pieces.forEach { board.add(it.first, it.second, it.third.loc()) }
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
            underTest.play("e2", "e4")
            assertEquals(Side.BLACK, underTest.info.turn)

            underTest.undoLast()

            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Piece.Pawn, underTest.board.at("e2".loc()))
            assertTrue(underTest.board.isEmpty("e4".loc()))
        }

        @Test
        fun `GIVEN game with checkmate WHEN undoLast THEN state becomes InProgress AND plies available`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("f2", "f3")
                .play("e7", "e6")
                .play("g2", "g4")
                .play("d8", "h4")
            assertEquals(Game.GameState.Finished(Result.CheckMate), underTest.state)

            underTest.undoLast()

            assertEquals(Game.GameState.InProgress, underTest.state)
            assertEquals(Side.BLACK, underTest.info.turn)
            assertTrue(underTest.plies().isNotEmpty())
        }

        @Test
        fun `GIVEN game with castling move WHEN undoLast THEN king AND rook return`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")
                .play("b8", "c6")
                .play("f1", "e2")
                .play("d7", "d6")
                .play("e1", "g1") // kingside castle

            assertEquals(Piece.King, underTest.board.at("g1".loc()))
            assertEquals(Piece.Rook, underTest.board.at("f1".loc()))

            underTest.undoLast()

            assertEquals(Piece.King, underTest.board.at("e1".loc()))
            assertEquals(Piece.Rook, underTest.board.at("h1".loc()))
            assertTrue(underTest.board.isEmpty("g1".loc()))
            assertTrue(underTest.board.isEmpty("f1".loc()))
        }

        @Test
        fun `GIVEN game with en passant WHEN undoLast THEN captured pawn returns`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("a7", "a6")
                .play("e4", "e5")
                .play("d7", "d5") // pawn moves 2 squares, enabling en passant
                .play("e5", "d6") // en passant capture

            assertTrue(underTest.board.isEmpty("d5".loc()))
            assertEquals(Piece.Pawn, underTest.board.at("d6".loc()))

            underTest.undoLast()

            assertEquals(Piece.Pawn, underTest.board.at("e5".loc()))
            assertEquals(Piece.Pawn, underTest.board.at("d5".loc()))
            assertTrue(underTest.board.isEmpty("d6".loc()))
        }

        @Test
        fun `GIVEN game with promotion WHEN undoLast THEN pawn returns AND promoted piece removed`() {
            val board = Board().apply {
                add(Piece.King, Side.WHITE, "e1".loc())
                add(Piece.King, Side.BLACK, "e8".loc())
                add(Piece.Pawn, Side.WHITE, "a7".loc())
            }
            underTest = MutableGame(board = board).apply { start() }
            val promotionPly = underTest.plies("a7".loc()).first { it.to == "a8".loc() }
            promotionPly.promote(Piece.Queen)
            underTest.play(promotionPly)

            assertEquals(Piece.Queen, underTest.board.at("a8".loc()))

            underTest.undoLast()

            assertEquals(Piece.Pawn, underTest.board.at("a7".loc()))
            assertTrue(underTest.board.isEmpty("a8".loc()))
        }

        @Test
        fun `GIVEN game after undoLast WHEN play different move THEN canReplay is false`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")

            underTest.undoLast()
            assertTrue(underTest.canReplay())

            underTest.play("d2", "d4")

            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game WHEN undoLast THEN lastPly highlights reflect previous move`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")

            underTest.undoLast()

            val lastPly = underTest.info.lastPly
            assertNotNull(lastPly)
            assertEquals("e7".loc(), lastPly!!.from)
            assertEquals("e5".loc(), lastPly.to)
        }

        @Test
        fun `GIVEN game with 1 move WHEN undoLast THEN lastPly is null`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")

            underTest.undoLast()

            assertNull(underTest.info.lastPly)
        }
    }

    @Nested
    internal inner class UndoAll {
        @Test
        fun `GIVEN a game WHEN undoing all THEN board is back to start`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")

            underTest.undoAll()

            assertFalse(underTest.canUndo())
            assertTrue(underTest.canReplay())
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(0, underTest.history.size)
            assertEquals(Piece.Pawn, underTest.board.at("e2".loc()))
            assertTrue(underTest.board.isEmpty("e4".loc()))
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
            underTest.play("f2", "f3")
                .play("e7", "e6")
                .play("g2", "g4")
                .play("d8", "h4")
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
            underTest.play("e2", "e4")
            underTest.undoLast()
            assertEquals(Side.WHITE, underTest.info.turn)

            underTest.replayNext()

            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Piece.Pawn, underTest.board.at("e4".loc()))
            assertTrue(underTest.board.isEmpty("e2".loc()))
        }

        @Test
        fun `GIVEN game at end WHEN canReplay THEN returns false`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")

            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game WHEN undo then replay THEN lastPly highlights are correct`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
            underTest.undoLast()

            underTest.replayNext()

            val lastPly = underTest.info.lastPly
            assertNotNull(lastPly)
            assertEquals("e7".loc(), lastPly!!.from)
            assertEquals("e5".loc(), lastPly.to)
        }
    }

    @Nested
    internal inner class ReplayAll {
        @Test
        fun `GIVEN game at start after undoAll WHEN replayAll THEN board matches end state`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")
            underTest.undoAll()

            underTest.replayAll()

            assertEquals(Side.BLACK, underTest.info.turn)
            assertEquals(Piece.Pawn, underTest.board.at("e4".loc()))
            assertEquals(Piece.Pawn, underTest.board.at("e5".loc()))
            assertEquals(Piece.Knight, underTest.board.at("f3".loc()))
            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game with no future WHEN replayAll THEN nothing changes`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
            val turnBefore = underTest.info.turn

            underTest.replayAll()

            assertEquals(turnBefore, underTest.info.turn)
            assertFalse(underTest.canReplay())
        }

        @Test
        fun `GIVEN game with multiple undos WHEN replayAll THEN history and board match original`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")
                .play("b8", "c6")

            val originalHistory = underTest.history.toList()
            underTest.undoAll()
            underTest.replayAll()

            assertEquals(originalHistory.size, underTest.history.size)
            assertEquals(Side.WHITE, underTest.info.turn)
            assertEquals(Piece.Knight, underTest.board.at("f3".loc()))
            assertEquals(Piece.Knight, underTest.board.at("c6".loc()))
        }
    }

    @Nested
    internal inner class HistoryBranching {
        @Test
        fun `GIVEN a game WHEN navigating back and playing a different move THEN future history is discarded`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
            underTest.play("e7", "e5")

            assertTrue(underTest.canUndo())
            underTest.undoLast()
            underTest.undoLast()

            assertTrue(underTest.canReplay())
            underTest.play("d2", "d4")

            assertFalse(underTest.canReplay())
            assertEquals(1, underTest.history.size)
        }

        @Test
        fun `GIVEN game with 3 moves WHEN undo 2 and play new THEN historySize is 1`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")

            underTest.undoLast()
            underTest.undoLast()
            underTest.play("d7", "d5")

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
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")

            assertEquals(3, underTest.historySize)
            assertEquals(3, underTest.currentMoveIndex)
        }

        @Test
        fun `GIVEN game undone by 1 WHEN checking currentMoveIndex THEN is historySize minus 1`() {
            underTest = MutableGame(board = on).apply { start() }
            underTest.play("e2", "e4")
                .play("e7", "e5")
                .play("g1", "f3")
            underTest.undoLast()

            assertEquals(3, underTest.historySize)
            assertEquals(2, underTest.currentMoveIndex)
        }
    }

    private fun MutableGame.play(from: String, to: String): MutableGame = apply {
        play(
            plies(from.loc()).firstOrNull { it.to == to.loc() }
                ?: throw AssertionError("Wrong move")
        )
    }
}
