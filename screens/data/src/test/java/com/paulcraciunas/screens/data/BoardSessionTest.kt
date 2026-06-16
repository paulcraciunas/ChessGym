package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BoardSessionTest {
    private val gameFactory = RealGameFactory()

    @Nested
    internal inner class Load {
        @Test
        fun `GIVEN a puzzle WHEN loaded THEN state has correct player`() {
            val session = loadPuzzleSession()

            assertEquals(Side.BLACK, session.current().player)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN state has correct rating`() {
            val session = loadPuzzleSession(rating = 1500)

            assertEquals(1500, session.current().rating)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN state has correct id`() {
            val session = loadPuzzleSession(id = 42)

            assertEquals(42, session.current().id)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN no outcome`() {
            val session = loadPuzzleSession()

            assertNull(session.current().outcome)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN no promotion`() {
            val session = loadPuzzleSession()

            assertNull(session.current().promotion)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN movePlayed is false`() {
            val session = loadPuzzleSession()

            assertFalse(session.current().movePlayed)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN captured pieces are empty`() {
            val session = loadPuzzleSession()

            assertEquals("", session.current().captured.byPlayer)
            assertEquals("", session.current().captured.byOpponent)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN first scripted move is played`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession().load(playable)

            // After initialize(), White played e2e4 — pawn is on e4 now
            assertNotNull(playable.board.at(Locus.e4))
            assertNull(playable.board.at(Locus.e2))
            assertNotNull(session.current().boardData.selection == null)
        }

        @Test
        fun `GIVEN a puzzle WHEN loaded THEN boardData has no selection`() {
            val session = loadPuzzleSession()

            assertNull(session.current().boardData.selection)
            assertTrue(session.current().boardData.availableMoves.isEmpty())
        }

        @Test
        fun `GIVEN a game WHEN loaded THEN state has correct player`() {
            val game = buildGame()
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession().load(playable)

            assertEquals(Side.WHITE, session.current().player)
        }

        @Test
        fun `GIVEN a game with rating WHEN loaded THEN state has rating`() {
            val game = buildGame(rating = 1800)
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession().load(playable)

            assertEquals(1800, session.current().rating)
        }

        @Test
        fun `GIVEN a game WHEN loaded THEN id is null`() {
            val game = buildGame()
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession().load(playable)

            assertNull(session.current().id)
        }
    }

    @Nested
    internal inner class OnClickSelection {
        @Test
        fun `GIVEN nothing selected WHEN clicking own piece THEN piece is selected`() {
            val session = loadPuzzleSession()

            val state = session.onClick(Locus.e7)

            assertEquals(Locus.e7, state.boardData.selection)
        }

        @Test
        fun `GIVEN nothing selected WHEN clicking own piece THEN available moves are populated`() {
            val session = loadPuzzleSession()

            val state = session.onClick(Locus.e7)

            assertTrue(state.boardData.availableMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN nothing selected WHEN clicking own piece THEN movePlayed is false`() {
            val session = loadPuzzleSession()

            val state = session.onClick(Locus.e7)

            assertFalse(state.movePlayed)
        }

        @Test
        fun `GIVEN nothing selected WHEN clicking empty square THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            val after = session.onClick(Locus.e5)

            assertEquals(before, after)
        }

        @Test
        fun `GIVEN nothing selected WHEN clicking opponent piece THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            // e4 has White's pawn after e2e4
            val after = session.onClick(Locus.e4)

            assertEquals(before, after)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking same piece THEN deselects`() {
            val session = loadPuzzleSession()
            session.onClick(Locus.e7)

            val state = session.onClick(Locus.e7)

            assertNull(state.boardData.selection)
            assertTrue(state.boardData.availableMoves.isEmpty())
        }

        @Test
        fun `GIVEN piece selected WHEN clicking same piece THEN movePlayed is false`() {
            val session = loadPuzzleSession()
            session.onClick(Locus.e7)

            val state = session.onClick(Locus.e7)

            assertFalse(state.movePlayed)
        }

        @Test
        fun `GIVEN knight selected WHEN checking available moves THEN moves include valid targets`() {
            val session = loadPuzzleSession()

            val state = session.onClick(Locus.b8)

            assertTrue(state.boardData.availableMoves.contains(Locus.c6))
            assertTrue(state.boardData.availableMoves.contains(Locus.a6))
        }
    }

    @Nested
    internal inner class OnClickTurnBased {
        @Test
        fun `GIVEN white played WHEN clicking black piece THEN piece is selected`() {
            val game = buildGame()
            game.start()
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = GameNavigation(game)).load(playable)

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            val state = session.onClick(Locus.e7)

            assertEquals(Locus.e7, state.boardData.selection)
        }

        @Test
        fun `GIVEN white played WHEN clicking white piece THEN state is unchanged`() {
            val game = buildGame()
            game.start()
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = GameNavigation(game)).load(playable)

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)
            val afterMove = session.current()

            val state = session.onClick(Locus.d2)

            assertEquals(afterMove, state)
        }

        @Test
        fun `GIVEN both sides played WHEN clicking white piece again THEN piece is selected`() {
            val game = buildGame()
            game.start()
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = GameNavigation(game)).load(playable)

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)
            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            val state = session.onClick(Locus.d2)

            assertEquals(Locus.d2, state.boardData.selection)
        }

        @Test
        fun `GIVEN free play WHEN alternating sides THEN player field stays as initial side`() {
            val game = buildGame()
            game.start()
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = GameNavigation(game)).load(playable)

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            assertEquals(Side.WHITE, session.current().player)
        }
    }

    @Nested
    internal inner class OnClickMoveExecution {
        @Test
        fun `GIVEN piece selected WHEN clicking valid target THEN move is played`() {
            val session = loadPuzzleSession()
            session.onClick(Locus.e7)

            val state = session.onClick(Locus.e5)

            assertNull(state.boardData.selection)
            assertTrue(state.movePlayed)
        }

        @Test
        fun `GIVEN piece selected WHEN clicking valid target THEN board is updated`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession().load(playable)
            session.onClick(Locus.e7)

            session.onClick(Locus.e5)

            assertNotNull(playable.board.at(Locus.e5))
            assertNull(playable.board.at(Locus.e7))
        }

        @Test
        fun `GIVEN piece selected WHEN clicking invalid target THEN deselects`() {
            val session = loadPuzzleSession()
            session.onClick(Locus.e7)

            // a3 is not a valid move for pawn on e7
            val state = session.onClick(Locus.a3)

            assertNull(state.boardData.selection)
            assertFalse(state.movePlayed)
        }

        @Test
        fun `GIVEN correct moves played WHEN puzzle completes THEN outcome is Won`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            // Opponent plays g1f3
            runTest { session.playOpponentMove() }

            session.onClick(Locus.b8)
            val finalState = session.onClick(Locus.c6)

            assertEquals(Outcome.Won, finalState.outcome)
            assertTrue(finalState.won)
        }
    }

    @Nested
    internal inner class OnClickAfterOutcome {
        @Test
        fun `GIVEN puzzle is over WHEN clicking any square THEN state is unchanged`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession().load(playable)

            playable.resign()
            val refreshed = session.refresh()

            val after = session.onClick(Locus.e7)

            assertEquals(refreshed, after)
        }

        @Test
        fun `GIVEN puzzle resigned WHEN clicking THEN outcome remains Lost`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession().load(playable)

            playable.resign()
            session.refresh()

            val state = session.onClick(Locus.e7)

            assertEquals(Outcome.Lost, state.outcome)
        }
    }

    @Nested
    internal inner class PromotionTests {
        @Test
        fun `GIVEN autoPromote true WHEN pawn reaches promotion rank THEN auto-promotes to Queen`() {
            val session = loadPromotionSession(autoPromote = true)

            session.onClick(Locus.a2)
            val state = session.onClick(Locus.a1)

            assertTrue(state.movePlayed)
            assertNull(state.promotion)
        }

        @Test
        fun `GIVEN autoPromote false WHEN pawn reaches promotion rank THEN shows promotion chooser`() {
            val session = loadPromotionSession(autoPromote = false)

            session.onClick(Locus.a2)
            val state = session.onClick(Locus.a1)

            val promotion = state.promotion
            assertNotNull(promotion)
            assertTrue(promotion!!.showChooser)
            assertEquals(Locus.a1, promotion.at)
        }

        @Test
        fun `GIVEN promotion chooser shown WHEN promoteIfPending called THEN promotion completes`() {
            val session = loadPromotionSession(autoPromote = false)

            session.onClick(Locus.a2)
            session.onClick(Locus.a1)

            val state = session.promoteIfPending(Piece.Rook)

            assertNull(state.promotion)
            assertTrue(state.movePlayed)
        }

        @Test
        fun `GIVEN no pending promotion WHEN promoteIfPending called THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            val after = session.promoteIfPending(Piece.Queen)

            assertEquals(before, after)
        }

        @Test
        fun `GIVEN promotion chooser shown WHEN promote called directly THEN promotion completes`() {
            val session = loadPromotionSession(autoPromote = false)

            session.onClick(Locus.a2)
            session.onClick(Locus.a1)

            val state = session.promote(Piece.Knight, Locus.a1)

            assertNull(state.promotion)
            assertTrue(state.movePlayed)
        }

        @Test
        fun `GIVEN no selection WHEN promote called THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            val after = session.promote(Piece.Queen, Locus.a1)

            assertEquals(before, after)
        }
    }

    @Nested
    internal inner class PlayOpponentMoveTests {
        @Test
        fun `GIVEN player has moved WHEN playOpponentMove THEN returns true`() = runTest {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            assertTrue(session.playOpponentMove())
        }

        @Test
        fun `GIVEN player has moved WHEN playOpponentMove THEN board is updated`() = runTest {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            session.playOpponentMove()

            // White knight should now be on f3 (g1f3)
            assertTrue(playable.board.has(Piece.Knight, Side.WHITE, Locus.f3))
        }

        @Test
        fun `GIVEN player has moved WHEN playOpponentMove THEN state has movePlayed true`() = runTest {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            session.playOpponentMove()
            val state = session.current()

            assertTrue(state.movePlayed)
        }

        @Test
        fun `GIVEN it is player turn WHEN playOpponentMove THEN returns false`() = runTest {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            // It's player's (Black's) turn right after load
            assertFalse(session.playOpponentMove())
        }

        @Test
        fun `GIVEN NoOpOpponent WHEN playOpponentMove THEN returns false`() = runTest {
            val session = loadPuzzleSession()

            assertFalse(session.playOpponentMove())
        }

        @Test
        fun `GIVEN opponent can play and not player turn WHEN canPlayOpponentMove THEN returns true`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            assertTrue(session.canPlayOpponentMove())
        }

        @Test
        fun `GIVEN player turn WHEN canPlayOpponentMove THEN returns false`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            assertFalse(session.canPlayOpponentMove())
        }
    }

    @Nested
    internal inner class NavigationTests {
        @Test
        fun `GIVEN GameNavigation WHEN canNavigate THEN returns true`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val session = BoardSession(navigation = navigation)
                .load(GamePlayableBoard(game, Side.WHITE))

            assertTrue(session.canNavigate())
        }

        @Test
        fun `GIVEN NoOpNavigation WHEN canNavigate THEN returns false`() {
            val session = loadPuzzleSession()

            assertFalse(session.canNavigate())
        }

        @Test
        fun `GIVEN no moves played WHEN canUndo THEN returns false`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val session = BoardSession(navigation = navigation)
                .load(GamePlayableBoard(game, Side.WHITE))

            assertFalse(session.canUndo())
        }

        @Test
        fun `GIVEN move played WHEN undoLast THEN board reverts`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = navigation).load(playable)

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            assertTrue(session.canUndo())

            val state = session.undoLast()

            assertFalse(state.movePlayed)
            assertNotNull(playable.board.at(Locus.e2))
        }

        @Test
        fun `GIVEN multiple moves played WHEN undoAll THEN board reverts to start`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = navigation).load(playable)

            game.play(Locus.e2, Locus.e4)
            game.play(Locus.e7, Locus.e5)
            session.refresh()

            session.undoAll()

            assertNotNull(playable.board.at(Locus.e2))
            assertNotNull(playable.board.at(Locus.e7))
        }

        @Test
        fun `GIVEN move undone WHEN replayNext THEN move is replayed`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = navigation).load(playable)

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)
            session.undoLast()

            assertTrue(session.canReplay())

            session.replayNext()

            assertNotNull(playable.board.at(Locus.e4))
        }

        @Test
        fun `GIVEN all moves undone WHEN replayAll THEN all moves are replayed`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val playable = GamePlayableBoard(game, Side.WHITE)
            val session = BoardSession(navigation = navigation).load(playable)

            game.play(Locus.e2, Locus.e4)
            game.play(Locus.e7, Locus.e5)
            session.refresh()
            session.undoAll()

            session.replayAll()

            assertNotNull(playable.board.at(Locus.e4))
            assertNotNull(playable.board.at(Locus.e5))
        }

        @Test
        fun `GIVEN no moves undone WHEN canReplay THEN returns false`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val session = BoardSession(navigation = navigation)
                .load(GamePlayableBoard(game, Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            assertFalse(session.canReplay())
        }

        @Test
        fun `GIVEN NoOpNavigation WHEN undoLast THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            val after = session.undoLast()

            assertEquals(before, after)
        }

        @Test
        fun `GIVEN NoOpNavigation WHEN replayNext THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            val after = session.replayNext()

            assertEquals(before, after)
        }

        @Test
        fun `GIVEN moves played WHEN completedMoves THEN returns correct count`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val session = BoardSession(navigation = navigation)
                .load(GamePlayableBoard(game, Side.WHITE))

            // historySize / 2 → 2 half-moves = 1 full move
            game.play(Locus.e2, Locus.e4)
            game.play(Locus.e7, Locus.e5)
            session.refresh()

            assertEquals(1, session.completedMoves())
        }

        @Test
        fun `GIVEN moves played WHEN algebraicHistory THEN returns non-empty string`() {
            val game = buildGame()
            val navigation = GameNavigation(game)
            val session = BoardSession(navigation = navigation)
                .load(GamePlayableBoard(game, Side.WHITE))

            session.onClick(Locus.e2)
            session.onClick(Locus.e4)

            assertTrue(session.algebraicHistory().isNotEmpty())
        }
    }

    @Nested
    internal inner class HintsAndSolutionTests {
        @Test
        fun `GIVEN puzzle with solution WHEN hint THEN selects expected square`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val solution = PuzzleSolution(puzzle)
            val session = BoardSession(solution = solution).load(playable)

            val state = session.hint()

            // Next expected move is e7e5, so hint selects e7
            assertEquals(Locus.e7, state.boardData.selection)
            assertTrue(state.boardData.availableMoves.isNotEmpty())
        }

        @Test
        fun `GIVEN NoOpSolution WHEN hint THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            val after = session.hint()

            assertEquals(before, after)
        }

        @Test
        fun `GIVEN puzzle with solution WHEN playNextSolutionMove THEN returns true`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val solution = PuzzleSolution(puzzle)
            val session = BoardSession(solution = solution).load(playable)

            assertTrue(session.playNextSolutionMove())
        }

        @Test
        fun `GIVEN puzzle with solution WHEN playNextSolutionMove THEN board advances`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val solution = PuzzleSolution(puzzle)
            val session = BoardSession(solution = solution).load(playable)

            session.playNextSolutionMove()

            // e7e5 was played (first player solution move)
            assertNotNull(playable.board.at(Locus.e5))
        }

        @Test
        fun `GIVEN NoOpSolution WHEN playNextSolutionMove THEN returns false`() {
            val session = loadPuzzleSession()

            assertFalse(session.playNextSolutionMove())
        }

        @Test
        fun `GIVEN all solution moves played WHEN playNextSolutionMove THEN returns false`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val solution = PuzzleSolution(puzzle)
            val session = BoardSession(solution = solution).load(playable)

            // Play all 3 remaining scripted moves: e7e5, g1f3, b8c6
            session.playNextSolutionMove()
            session.playNextSolutionMove()
            session.playNextSolutionMove()

            assertFalse(session.playNextSolutionMove())
        }
    }

    @Nested
    internal inner class ClearAndRefreshTests {
        @Test
        fun `GIVEN piece selected WHEN clear THEN selection is removed`() {
            val session = loadPuzzleSession()
            session.onClick(Locus.e7)

            val state = session.clear()

            assertNull(state.boardData.selection)
            assertTrue(state.boardData.availableMoves.isEmpty())
        }

        @Test
        fun `GIVEN no selection WHEN clear THEN state is unchanged`() {
            val session = loadPuzzleSession()
            val before = session.current()

            val after = session.clear()

            assertEquals(before.boardData.selection, after.boardData.selection)
        }

        @Test
        fun `GIVEN session WHEN refresh THEN movePlayed is false`() {
            val session = loadPuzzleSession()
            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            val state = session.refresh()

            assertFalse(state.movePlayed)
        }

        @Test
        fun `GIVEN session WHEN refresh with animation THEN boardData is reloaded`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession().load(playable)
            session.onClick(Locus.e7)
            session.onClick(Locus.e5)

            val state = session.refresh(withAnimation = true)

            assertNotNull(state.boardData.animatingPiece)
        }

        @Test
        fun `GIVEN session WHEN refresh without animation THEN no animating piece`() {
            val session = loadPuzzleSession()

            val state = session.refresh(withAnimation = false)

            assertNull(state.boardData.animatingPiece)
        }
    }

    @Nested
    internal inner class CloseTests {
        @Test
        fun `GIVEN session with opponent WHEN close THEN shutdown is called`() = runTest {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            session.close()
        }

        @Test
        fun `GIVEN session with NoOpOpponent WHEN close THEN completes without error`() = runTest {
            val session = loadPuzzleSession()

            session.close()
        }
    }

    @Nested
    internal inner class ResultTests {
        @Test
        fun `GIVEN puzzle won WHEN result THEN returns Won outcome`() = runTest {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val opponent = ScriptedOpponent(puzzle)
            val session = BoardSession(opponent = opponent).load(playable)

            session.onClick(Locus.e7)
            session.onClick(Locus.e5)
            session.playOpponentMove()
            session.onClick(Locus.b8)
            session.onClick(Locus.c6)

            val result = session.result()

            assertEquals(Outcome.Won, result.outcome)
            assertTrue(result.success)
            assertEquals(1200, result.rating)
            assertEquals(1, result.id)
        }

        @Test
        fun `GIVEN puzzle lost WHEN result THEN returns Lost outcome`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession().load(playable)

            playable.resign()
            session.refresh()

            val result = session.result()

            assertEquals(Outcome.Lost, result.outcome)
            assertFalse(result.success)
        }

        @Test
        fun `GIVEN puzzle with wrong move WHEN result THEN returns Lost outcome`() {
            val puzzle = buildPuzzle()
            val playable = PuzzlePlayableBoard(puzzle)
            val session = BoardSession().load(playable)

            // d7d5 is wrong — expected e7e5
            session.onClick(Locus.d7)
            session.onClick(Locus.d5)

            val result = session.result()

            assertEquals(Outcome.Lost, result.outcome)
        }
    }

    private fun loadPuzzleSession(
        rating: Int = 1200,
        id: Int? = 1,
    ): BoardSession {
        val puzzle = buildPuzzle(rating = rating, id = id)
        val playable = PuzzlePlayableBoard(puzzle)
        return BoardSession().load(playable)
    }

    private fun loadPromotionSession(autoPromote: Boolean): BoardSession {
        val puzzle = buildPromotionPuzzle()
        val playable = PuzzlePlayableBoard(puzzle)
        return BoardSession().apply { this.autoPromote = autoPromote }.load(playable)
    }

    private fun buildPuzzle(
        rating: Int = 1200,
        id: Int? = 1,
        moves: List<String> = listOf("e2e4", "e7e5", "g1f3", "b8c6"),
    ): Puzzle = gameFactory.builder()
        .withId(id)
        .withDefaultBoard()
        .withRating(rating)
        .withMoves(moves)
        .buildPuzzle()

    // Black pawn on a2 can promote to a1 after White's g2g3
    private fun buildPromotionPuzzle(): Puzzle = gameFactory.builder()
        .withId(1)
        .withTurn(Side.WHITE)
        .withPiece(Piece.King, Side.WHITE, Locus.e1)
        .withPiece(Piece.Pawn, Side.WHITE, Locus.g2)
        .withPiece(Piece.King, Side.BLACK, Locus.e8)
        .withPiece(Piece.Pawn, Side.BLACK, Locus.a2)
        .withRating(1200)
        .withMoves(listOf("g2g3", "a2a1"))
        .buildPuzzle()

    private fun buildGame(rating: Int? = null) = gameFactory.builder()
        .withDefaultBoard()
        .apply { rating?.let { withRating(it) } }
        .buildGame()
}
