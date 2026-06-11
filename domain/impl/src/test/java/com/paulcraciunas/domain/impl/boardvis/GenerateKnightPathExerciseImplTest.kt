package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.general.SequentialRandomFactory
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GenerateKnightPathExerciseImplTest {
    private val randomFactory = SequentialRandomFactory()
    private val underTest = GenerateKnightPathExerciseImpl(randomFactory)

    @Test
    fun `GIVEN movesRequired 2 WHEN invoke THEN exercise has correct path length`() {
        val exercise = underTest(2)

        assertEquals(2, exercise.movesRequired)
        assertEquals(3, exercise.path.size)
    }

    @Test
    fun `GIVEN movesRequired 3 WHEN invoke THEN exercise has correct path length`() {
        val exercise = underTest(3)

        assertEquals(3, exercise.movesRequired)
        assertEquals(4, exercise.path.size)
    }

    @Test
    fun `GIVEN exercise WHEN checking path THEN path ends at destination`() {
        val exercise = underTest(2)

        assertEquals(exercise.destination, exercise.path.last())
    }

    @Test
    fun `GIVEN exercise WHEN checking path THEN each step is a valid knight move`() {
        val exercise = underTest(3)

        for (i in 0 until exercise.path.size - 1) {
            val from = exercise.path[i]
            val to = exercise.path[i + 1]
            val validMoves = GenerateKnightPathExerciseImpl.knightMoves(from)
            assertTrue(to in validMoves) { "$to is not a valid knight move from $from" }
        }
    }

    @Test
    fun `GIVEN exercise WHEN checking board THEN has knight at start`() {
        val exercise = underTest(2)

        assertEquals(Piece.Knight, exercise.board.at(exercise.path.first()))
        assertTrue(exercise.board.has(Side.WHITE, exercise.path.first()))
    }

    @Test
    fun `GIVEN exercise WHEN checking board THEN has destination pawn`() {
        val exercise = underTest(2)

        assertEquals(Piece.Pawn, exercise.board.at(exercise.destination))
        assertTrue(exercise.board.has(Side.BLACK, exercise.destination))
    }

    @Test
    fun `GIVEN exercise WHEN checking blockers THEN they only block alternative shortest path squares`() {
        val exercise = underTest(3)
        val start = exercise.path.first()
        val destination = exercise.destination
        val totalDist = exercise.movesRequired

        val distFromStart = fullBfs(start)
        val distFromDest = fullBfs(destination)

        val pathSet = exercise.path.toSet()
        val blockedSquares = mutableSetOf<Locus>()
        exercise.board.forEachPiece(turn = Side.WHITE) { piece, locus ->
            if (piece == Piece.Pawn) blockedSquares.add(locus)
        }

        for (blocker in blockedSquares) {
            val dfs = distFromStart[blocker]!!
            val dfd = distFromDest[blocker]!!
            assertTrue(dfs + dfd == totalDist) {
                "Blocker at $blocker is not on any shortest path (dist: $dfs + $dfd != $totalDist)"
            }
            assertTrue(blocker !in pathSet) {
                "Blocker at $blocker should not be on the chosen path"
            }
        }
    }

    @Test
    fun `GIVEN exercise WHEN checking board THEN all alternative shortest-path branches are blocked`() {
        val exercise = underTest(2)
        val start = exercise.path.first()
        val destination = exercise.destination
        val totalDist = exercise.movesRequired

        val distFromStart = fullBfs(start)
        val distFromDest = fullBfs(destination)
        val pathSet = exercise.path.toSet()

        for (i in 0 until exercise.path.size - 1) {
            val current = exercise.path[i]
            val nextOnPath = exercise.path[i + 1]
            val currentDistFromStart = distFromStart[current]!!

            for (move in GenerateKnightPathExerciseImpl.knightMoves(current)) {
                if (move == nextOnPath || move in pathSet) continue

                val dfs = distFromStart[move] ?: continue
                val dfd = distFromDest[move] ?: continue

                if (dfs == currentDistFromStart + 1 && dfs + dfd == totalDist) {
                    assertNotNull(exercise.board.at(move)) {
                        "Alternative shortest-path branch $move from $current should be blocked"
                    }
                }
            }
        }
    }

    @Test
    fun `GIVEN exercise WHEN start and destination THEN they are different squares`() {
        val exercise = underTest(2)

        assertNotEquals(exercise.path.first(), exercise.destination)
    }

    @Test
    fun `GIVEN movesRequired below minimum WHEN invoke THEN asserts`() {
        assertThrows<AssertionError> {
            underTest(1)
        }
    }

    @Test
    fun `GIVEN movesRequired above maximum WHEN invoke THEN asserts`() {
        assertThrows<AssertionError> {
            underTest(7)
        }
    }

    @Test
    fun `GIVEN knightMoves WHEN called from center THEN returns 8 moves`() {
        val moves = GenerateKnightPathExerciseImpl.knightMoves(Locus.d4)

        assertEquals(8, moves.size)
    }

    @Test
    fun `GIVEN knightMoves WHEN called from corner THEN returns 2 moves`() {
        val moves = GenerateKnightPathExerciseImpl.knightMoves(Locus.a1)

        assertEquals(2, moves.size)
        assertTrue(Locus.b3 in moves)
        assertTrue(Locus.c2 in moves)
    }

    @Test
    fun `GIVEN movesRequired 4 WHEN invoke THEN exercise is valid`() {
        val exercise = underTest(4)

        assertEquals(4, exercise.movesRequired)
        assertEquals(5, exercise.path.size)
    }

    @Test
    fun `GIVEN movesRequired 5 WHEN invoke THEN exercise is valid`() {
        val exercise = underTest(5)

        assertEquals(5, exercise.movesRequired)
        assertEquals(6, exercise.path.size)
    }

    @Test
    fun `GIVEN movesRequired 6 WHEN invoke THEN exercise is valid`() {
        val exercise = underTest(6)

        assertEquals(6, exercise.movesRequired)
        assertEquals(7, exercise.path.size)
    }

    private fun fullBfs(start: Locus): Map<Locus, Int> {
        val distance = mutableMapOf(start to 0)
        val queue = ArrayDeque<Locus>()
        queue.add(start)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (neighbor in GenerateKnightPathExerciseImpl.knightMoves(current)) {
                if (neighbor !in distance) {
                    distance[neighbor] = distance[current]!! + 1
                    queue.add(neighbor)
                }
            }
        }
        return distance
    }
}
