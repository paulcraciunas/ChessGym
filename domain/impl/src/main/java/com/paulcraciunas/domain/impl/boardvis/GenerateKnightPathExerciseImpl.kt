package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.boardvis.GenerateKnightPathExercise
import com.paulcraciunas.domain.api.boardvis.KnightPathExercise
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.logic.builders.Builders
import javax.inject.Inject

class GenerateKnightPathExerciseImpl @Inject constructor(
    private val randomFactory: RandomFactory,
) : GenerateKnightPathExercise {

    override fun invoke(movesRequired: Int): KnightPathExercise {
        assert(movesRequired in MIN_MOVES..MAX_MOVES)

        val start = Locus.entries[randomFactory.nextInt(0, Locus.entries.size)]
        val distFromStart = bfs(start, movesRequired)

        val candidates = Locus.entries.filter { distFromStart[it] == movesRequired }
        val destination = candidates[randomFactory.nextInt(0, candidates.size)]

        val distFromDest = bfs(destination, movesRequired)
        val path = tracePath(destination, distFromStart)
        val blockers = computeBlockers(path, distFromStart, distFromDest, movesRequired)
        val board = buildBoard(start, destination, blockers)

        return KnightPathExercise(
            board = board,
            destination = destination,
            path = path,
            movesRequired = movesRequired,
        )
    }

    private fun bfs(start: Locus, maxDistance: Int): Map<Locus, Int> {
        val distance = mutableMapOf(start to 0)
        val queue = ArrayDeque<Locus>()
        queue.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDist = distance[current]!!
            if (currentDist >= maxDistance) continue

            for (neighbor in knightMoves(current)) {
                if (neighbor !in distance) {
                    distance[neighbor] = currentDist + 1
                    queue.add(neighbor)
                }
            }
        }
        return distance
    }

    private fun tracePath(destination: Locus, distFromStart: Map<Locus, Int>): List<Locus> {
        val totalMoves = distFromStart[destination]!!
        val path = mutableListOf(destination)
        var current = destination

        for (stepsRemaining in totalMoves - 1 downTo 0) {
            val predecessors = knightMoves(current).filter {
                distFromStart[it] == stepsRemaining
            }
            current = predecessors[randomFactory.nextInt(0, predecessors.size)]
            path.add(current)
        }

        return path.reversed()
    }

    /**
     * Blocks only squares where the player could branch onto an alternative
     * shortest path. A square X is on some shortest path iff
     * distFromStart(x) + distFromDest(x) == totalDistance.
     *
     * For each step on the chosen path, we look at knight moves that lead to
     * a shortest-path square in the next distance layer (i.e. a valid branching
     * point for a competing shortest path). Only those get blocked.
     *
     * Non-shortest moves remain available: the player can still make "bad"
     * moves that would increase the total distance.
     */
    private fun computeBlockers(
        path: List<Locus>,
        distFromStart: Map<Locus, Int>,
        distFromDest: Map<Locus, Int>,
        totalDistance: Int,
    ): Set<Locus> {
        val pathSet = path.toSet()
        val blockers = mutableSetOf<Locus>()

        for (i in 0 until path.size - 1) {
            val current = path[i]
            val nextOnPath = path[i + 1]
            val currentDistFromStart = distFromStart[current]!!

            for (move in knightMoves(current)) {
                if (move == nextOnPath) continue
                if (move in pathSet) continue

                val dfs = distFromStart[move] ?: continue
                val dfd = distFromDest[move] ?: continue

                if (dfs == currentDistFromStart + 1 && dfs + dfd == totalDistance) {
                    blockers.add(move)
                }
            }
        }
        return blockers
    }

    private fun buildBoard(knightStart: Locus, destination: Locus, blockers: Set<Locus>): IBoard =
        Builders.boardFactory().emptyBoard().apply {
            add(Piece.Knight, Side.WHITE, knightStart)
            blockers.forEach { add(Piece.Pawn, Side.WHITE, it) }
            add(Piece.Pawn, Side.BLACK, destination)
        }

    companion object {
        private const val MIN_MOVES = 2
        private const val MAX_MOVES = 6

        private val KNIGHT_OFFSETS = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1,
        )

        internal fun knightMoves(from: Locus): List<Locus> {
            val fileIdx = from.file.ordinal
            val rankIdx = from.rank.ordinal
            return KNIGHT_OFFSETS.mapNotNull { (df, dr) ->
                val f = fileIdx + df
                val r = rankIdx + dr
                if (f in 0..7 && r in 0..7) {
                    Locus.from(File.entries[f], Rank.entries[r])
                } else null
            }
        }
    }
}
