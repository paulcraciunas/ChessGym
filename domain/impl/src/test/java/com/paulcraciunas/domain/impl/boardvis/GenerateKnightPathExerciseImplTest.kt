package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.general.FixedRandomFactory
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.general.SequentialRandomFactory
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GenerateKnightPathExerciseImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private val randomFactory = SequentialRandomFactory()
    private val underTest = GenerateKnightPathExerciseImpl(testDispatcher, randomFactory)

    @Test
    fun `GIVEN movesRequired 2 WHEN invoke THEN exercise has correct path length`() = runTest(testDispatcher) {
        val exercise = underTest(2)

        assertEquals(3, exercise.path.size)
    }

    @Test
    fun `GIVEN movesRequired 3 WHEN invoke THEN exercise has correct path length`() = runTest(testDispatcher) {
        val exercise = underTest(3)

        assertEquals(4, exercise.path.size)
    }

    @Test
    fun `GIVEN exercise WHEN checking path THEN path starts at from`() = runTest(testDispatcher) {
        val exercise = underTest(2)

        assertEquals(exercise.from, exercise.path.first())
    }

    @Test
    fun `GIVEN exercise WHEN checking board THEN has knight at start`() = runTest(testDispatcher) {
        val exercise = underTest(2)

        assertEquals(Piece.Knight, exercise.board.at(exercise.path.first()))
        assertTrue(exercise.board.has(Side.WHITE, exercise.path.first()))
    }

    @Test
    fun `GIVEN movesRequired below minimum WHEN invoke THEN exercise has correct path length`() = runTest(testDispatcher) {
        val exercise = underTest(1)

        assertEquals(3, exercise.path.size)
    }

    @Test
    fun `GIVEN movesRequired above maximum WHEN invoke THEN exercise has correct path length`() = runTest(testDispatcher) {
        val exercise = underTest(10)

        assertEquals(7, exercise.path.size)
    }

    @Test
    fun `WHEN invoke THEN correctly populate pieces`() = runTest(testDispatcher) {
        val exercise = underTest.invoke(3)

        assertTrue(exercise.board.has(Piece.Knight, Side.WHITE, exercise.from))
        assertEquals(1, exercise.board.pieces(Side.BLACK, Piece.Pawn).size)
    }

    @Test
    fun `GIVEN different movesRequired WHEN invoke THEN white pawns do not block the golden path`() = runTest(testDispatcher) {
        // Run against multiple difficulty variations using Sequential to diversify paths
        for (difficulty in 2..6) {
            val exercise = underTest.invoke(difficulty)
            val goldenPathSquares = exercise.path.toSet()

            for (locus in goldenPathSquares) {
                // Ensure no white blocking pawns clash with where the player needs to hop
                val hasBlocker = exercise.board.has(Piece.Pawn, Side.WHITE, locus)
                assertTrue(!hasBlocker, "Golden Path square $locus must remain unblocked!")
            }
        }
    }

    @Test
    fun `GIVEN identical conditions WHEN invoke THEN generates 100 percent deterministic output`() = runTest(testDispatcher) {
        // Fixed factory zero guarantees identical random picks for destination, starts, and path tracks
        val factory1 = FixedRandomFactory(0)
        val useCase1 = GenerateKnightPathExerciseImpl(testDispatcher, factory1)
        val exercise1 = useCase1.invoke(4)

        val factory2 = FixedRandomFactory(0)
        val useCase2 = GenerateKnightPathExerciseImpl(testDispatcher, factory2)
        val exercise2 = useCase2.invoke(4)

        // Complete structural equality check across consecutive generations
        assertEquals(exercise1.from, exercise2.from)
        assertEquals(exercise1.path, exercise2.path)

        assertTrue {
            exercise1.board.pieces(Side.WHITE, Piece.Pawn).containsAll(exercise2.board.pieces(Side.WHITE, Piece.Pawn))
                && exercise2.board.pieces(Side.WHITE, Piece.Pawn).containsAll(exercise1.board.pieces(Side.WHITE, Piece.Pawn))
        }
    }

    @Test
    fun `GIVEN longest movesRequired WHEN invoke THEN success`() = runTest(testDispatcher) {
        // 6-move puzzles yield the highest numbers of overlapping paths.
        // This test acts as a regression/performance guard against backtracking execution loops.
        val exercise = underTest.invoke(6)

        assertEquals(7, exercise.path.size)
    }

    @Test
    fun `GIVEN 2-move exercise with multiple paths WHEN invoke THEN intermediate alternative steps are strictly blocked`() = runTest(testDispatcher) {
        val configurationFactory = object : RandomFactory {
            private var callCount = 0
            override fun nextInt(from: Int, to: Int): Int {
                callCount++
                return when (callCount) {
                    1 -> Locus.entries.indexOf(Locus.b2) // First call: Choose Locus.b2 as the target destination
                    2 -> 7 // This will correspond to e5
                    else -> 0 // Third call: Choose the first path as the Golden Path (e5 -> c4 -> b2)
                }
            }
        }

        val underTest = GenerateKnightPathExerciseImpl(
            dispatcher = testDispatcher,
            randomFactory = configurationFactory
        )

        val exercise = underTest.invoke(2)

        // Assert the structural setup matched our intended test profile
        assertEquals(Locus.e5, exercise.from)
        assertEquals(Locus.c4, exercise.path[1]) // Golden Path step

        assertTrue(exercise.board.has(Piece.Pawn, Side.WHITE, Locus.d3))
    }
}
