package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class BoardSessionTest {
    private val gameFactory = RealGameFactory()

    @Nested
    internal inner class LoadWithGame {
        @Test
        fun `GIVEN game WHEN load THEN emits BoardState with correct player`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertEquals(Side.WHITE, session.currentState.player)
        }

        @Test
        fun `GIVEN game WHEN load as black THEN emits BoardState with black player`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.BLACK))
            assertEquals(Side.BLACK, session.currentState.player)
        }

        @Test
        fun `GIVEN game WHEN load THEN emits BoardState with no captured pieces`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertEquals("", session.currentState.captured.byPlayer)
            assertEquals("", session.currentState.captured.byOpponent)
        }

        @Test
        fun `GIVEN game WHEN load THEN isLoaded is true`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertTrue(session.isLoaded())
        }

        @Test
        fun `GIVEN game with rating WHEN load THEN rating is in state`() {
            val game = gameFactory.builder().withDefaultBoard().withRating(1600).buildGame()
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))
            assertEquals(1600, session.currentState.rating)
        }

        @Test
        fun `GIVEN game WHEN load THEN interactive is true`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            assertTrue(session.currentState.interactive)
        }
    }

    @Nested
    internal inner class LoadWithPuzzle {
        @Test
        fun `GIVEN puzzle WHEN load THEN emits correct player and rating`() {
            val session = BoardSession()
            val scope = TestScope()
            val puzzle = buildStandardPuzzle(rating = 1500)
            session.load(scope, PuzzlePlayableBoard(puzzle))
            assertEquals(Side.BLACK, session.currentState.player)
            assertEquals(1500, session.currentState.rating)
        }

        @Test
        fun `GIVEN puzzle WHEN load THEN id is accessible`() {
            val puzzle = gameFactory.builder()
                .withId(42)
                .withDefaultBoard()
                .withRating(1200)
                .withMoves(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
                .buildPuzzle()
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, PuzzlePlayableBoard(puzzle))
            assertEquals(42, session.currentState.id)
        }
    }

    @Nested
    internal inner class OnClick {
        @Test
        fun `GIVEN no selection WHEN clicking own piece THEN piece is selected`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            session.onClick(Locus.e2)

            val state = session.currentState
            assertEquals(Locus.e2, state.boardData.selection)
            assertTrue(state.boardData.availableMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN no selection WHEN clicking opponent piece THEN not selected`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            session.onClick(Locus.e7)

            val state = session.currentState
            assertNull(state.boardData.selection)
            assertFalse(state.movePlayed)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking same square THEN clears selection`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            session.onClick(Locus.e2)

            session.onClick(Locus.e2)

            assertNull(session.currentState.boardData.selection)
        }

        @Test
        fun `GIVEN piece selected WHEN valid move THEN move is played`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            session.onClick(Locus.e2)

            session.onClick(Locus.e4)

            val state = session.currentState
            assertTrue(state.movePlayed)
            assertNotNull(state.boardData.at(Locus.e4).piece)
            assertEquals(Piece.Pawn, state.boardData.at(Locus.e4).piece?.piece?.piece)
        }

        @Test
        fun `GIVEN not interactive WHEN onClick THEN ignored`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))
            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            assertEquals(Locus.e4, session.currentState.boardData.animatingPiece?.to)
        }

        @Test
        fun `GIVEN empty square WHEN onClick THEN no selection`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            session.onClick(Locus.e4)

            assertNull(session.currentState.boardData.selection)
            assertFalse(session.currentState.movePlayed)
        }
    }

    @Nested
    internal inner class OpponentIntegration {
        @Test
        fun `GIVEN scripted opponent WHEN player moves THEN opponent auto-responds`() = runTest {
            val puzzle = buildStandardPuzzle()
            val opponent = ScriptedOpponent().apply { load(puzzle) }
            val session = BoardSession(opponent = opponent)
            session.load(this, PuzzlePlayableBoard(puzzle))

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)
            advanceUntilIdle()

            val state = session.currentState
            assertTrue(state.interactive)
            assertNotNull(state.boardData.at(Locus.f3).piece)
        }

        @Test
        fun `GIVEN scripted opponent WHEN player makes correct moves THEN puzzle completes`() = runTest {
            val puzzle = buildStandardPuzzle()
            val opponent = ScriptedOpponent().apply { load(puzzle) }
            val session = BoardSession(opponent = opponent)
            session.load(this, PuzzlePlayableBoard(puzzle))

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)
            advanceUntilIdle()

            session.onClick(Locus.b8)
            session.onClick(Locus.c6)
            advanceUntilIdle()

            assertTrue(session.currentState.isOver)
            assertEquals(Outcome.Won, session.currentState.outcome)
        }

        @Test
        fun `GIVEN NoOpOpponent WHEN player moves THEN no opponent response`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            val state = session.currentState
            assertTrue(state.interactive)
            assertTrue(state.movePlayed)
        }
    }

    @Nested
    internal inner class NavigationWithGame {
        @Test
        fun `GIVEN game at start WHEN canUndo THEN returns false`() {
            val game = buildDefaultGame()
            val session = BoardSession(navigation = GameNavigation().apply { load(game) })
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))

            assertFalse(session.canUndo())
        }

        @Test
        fun `GIVEN game with moves WHEN undoLast THEN reflects previous position`() {
            val game = buildDefaultGame()
            val navigation = GameNavigation().apply { load(game) }
            val session = BoardSession(navigation = navigation)
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))
            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            session.undoLast()

            assertNotNull(session.currentState.boardData.at(Locus.e2).piece)
            assertNull(session.currentState.boardData.at(Locus.e4).piece)
        }
    }

    @Nested
    internal inner class SolutionWithPuzzle {
        @Test
        fun `GIVEN puzzle WHEN hint THEN hint square is selected`() {
            val puzzle = buildStandardPuzzle()
            val solution = PuzzleSolution().apply { load(puzzle) }
            val session = BoardSession(solution = solution)
            val scope = TestScope()
            session.load(scope, PuzzlePlayableBoard(puzzle))

            session.hint()

            val state = session.currentState
            assertEquals(Locus.e7, state.boardData.selection)
        }

        @Test
        fun `GIVEN puzzle WHEN playNextSolutionMove THEN plays move`() {
            val puzzle = buildStandardPuzzle()
            val solution = PuzzleSolution().apply { load(puzzle) }
            val session = BoardSession(solution = solution)
            val scope = TestScope()
            session.load(scope, PuzzlePlayableBoard(puzzle))

            assertTrue(session.playNextSolutionMove())
            assertNotNull(session.currentState.boardData.at(Locus.e5).piece)
        }
    }

    @Nested
    internal inner class LastMoveHighlights {
        @Test
        fun `GIVEN game at start WHEN loaded THEN no last move highlights`() {
            val game = buildDefaultGame()
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))

            val boardData = session.currentState.boardData
            assertFalse(boardData.at(Locus.e2).lastMove)
            assertFalse(boardData.at(Locus.e4).lastMove)
        }

        @Test
        fun `GIVEN move played WHEN state read THEN from and to are highlighted`() {
            val game = buildDefaultGame()
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            val boardData = session.currentState.boardData
            assertTrue(boardData.at(Locus.e2).lastMove)
            assertTrue(boardData.at(Locus.e4).lastMove)
            assertFalse(boardData.at(Locus.d2).lastMove)
        }

        @Test
        fun `GIVEN two moves WHEN undoLast THEN highlights match first move`() {
            val game = buildDefaultGame()
            val navigation = GameNavigation().apply { load(game) }
            val session = BoardSession(navigation = navigation, opponent = NoOpOpponent)
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)
            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            session.undoLast()

            val boardData = session.currentState.boardData
            assertTrue(boardData.at(Locus.e2).lastMove)
            assertTrue(boardData.at(Locus.e4).lastMove)
            assertFalse(boardData.at(Locus.e7).lastMove)
            assertFalse(boardData.at(Locus.e5).lastMove)
        }

        @Test
        fun `GIVEN moves played WHEN undoAll THEN no last move highlights`() {
            val game = buildDefaultGame()
            val navigation = GameNavigation().apply { load(game) }
            val session = BoardSession(navigation = navigation, opponent = NoOpOpponent)
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)
            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            session.undoAll()

            val boardData = session.currentState.boardData
            assertFalse(boardData.at(Locus.e2).lastMove)
            assertFalse(boardData.at(Locus.e4).lastMove)
            assertFalse(boardData.at(Locus.e7).lastMove)
            assertFalse(boardData.at(Locus.e5).lastMove)
        }

        @Test
        fun `GIVEN undone move WHEN replayNext THEN highlights restored`() {
            val game = buildDefaultGame()
            val navigation = GameNavigation().apply { load(game) }
            val session = BoardSession(navigation = navigation, opponent = NoOpOpponent)
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)
            session.undoLast()

            session.replayNext()

            val boardData = session.currentState.boardData
            assertTrue(boardData.at(Locus.e2).lastMove)
            assertTrue(boardData.at(Locus.e4).lastMove)
        }

        @Test
        fun `GIVEN two moves WHEN undoLast then replayNext THEN highlights match second move`() {
            val game = buildDefaultGame()
            val navigation = GameNavigation().apply { load(game) }
            val session = BoardSession(navigation = navigation, opponent = NoOpOpponent)
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(game, Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)
            session.onClick(Locus.e7)
            session.onClick(Locus.e5)
            session.undoAll()

            session.replayNext()
            val afterFirst = session.currentState.boardData
            assertTrue(afterFirst.at(Locus.e2).lastMove)
            assertTrue(afterFirst.at(Locus.e4).lastMove)

            session.replayNext()
            val afterSecond = session.currentState.boardData
            assertTrue(afterSecond.at(Locus.e7).lastMove)
            assertTrue(afterSecond.at(Locus.e5).lastMove)
            assertFalse(afterSecond.at(Locus.e2).lastMove)
            assertFalse(afterSecond.at(Locus.e4).lastMove)
        }
    }

    @Nested
    internal inner class Resign {
        @Test
        fun `GIVEN playing WHEN resign THEN game is over with Loss`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildDefaultGame(), Side.WHITE))

            session.resign()

            assertTrue(session.currentState.isOver)
            assertEquals(Outcome.Lost, session.currentState.outcome)
        }
    }

    @Nested
    internal inner class Promotion {
        @Test
        fun `GIVEN auto-promote WHEN pawn reaches back rank THEN promotes to queen`() {
            val session = BoardSession()
            session.autoPromote = true
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildPromotionGame(), Side.WHITE))

            session.onClick(Locus.a7)
            session.onClick(Locus.a8)

            val state = session.currentState
            assertTrue(state.movePlayed)
            assertNull(state.promotion)
            assertEquals(Piece.Queen, state.boardData.at(Locus.a8).piece?.piece?.piece)
        }

        @Test
        fun `GIVEN no auto-promote WHEN pawn reaches back rank THEN shows promotion chooser`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildPromotionGame(), Side.WHITE))

            session.onClick(Locus.a7)
            session.onClick(Locus.a8)

            val state = session.currentState
            assertNotNull(state.promotion)
            assertTrue(state.promotion!!.showChooser)
            assertEquals(Locus.a8, state.promotion.at)
        }

        @Test
        fun `GIVEN promotion pending WHEN promote to knight THEN knight on board`() {
            val session = BoardSession()
            val scope = TestScope()
            session.load(scope, GamePlayableBoard(buildPromotionGame(), Side.WHITE))
            session.onClick(Locus.a7)
            session.onClick(Locus.a8)

            session.promote(Piece.Knight, Locus.a8)

            val state = session.currentState
            assertEquals(Piece.Knight, state.boardData.at(Locus.a8).piece?.piece?.piece)
            assertTrue(state.movePlayed)
            assertNull(state.promotion)
        }
    }

    private fun buildDefaultGame(): Game = gameFactory.builder()
        .withDefaultBoard()
        .buildGame()

    private fun buildPromotionGame(): Game = gameFactory.builder()
        .withTurn(Side.WHITE)
        .withPiece(Piece.King, Side.WHITE, Locus.e1)
        .withPiece(Piece.King, Side.BLACK, Locus.e8)
        .withPiece(Piece.Pawn, Side.WHITE, Locus.a7)
        .buildGame()

    private fun buildStandardPuzzle(rating: Int = 1200): Puzzle = gameFactory.builder()
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(listOf("e2e4", "e7e5", "g1f3", "b8c6"))
        .buildPuzzle()
}
