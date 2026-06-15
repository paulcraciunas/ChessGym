package com.paulcraciunas.domain.impl.boardvis

import com.paulcraciunas.domain.api.boardvis.GenerateKnightPathExercise
import com.paulcraciunas.domain.api.boardvis.KnightPathExercise
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.global.qualifiers.DefaultDispatcher
import com.paulcraciunas.logic.builders.Builders
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.LinkedList
import javax.inject.Inject

class GenerateKnightPathExerciseImpl @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val randomFactory: RandomFactory,
) : GenerateKnightPathExercise {

    override suspend fun invoke(movesRequired: Int): KnightPathExercise = withContext(dispatcher) {
        val moves = movesRequired.coerceIn(MIN_MOVES, MAX_MOVES)
        // Generate a valid start/end pair deterministically
        val (startLocus, endLocus, allPaths) = findValidExercise(moves)

        // Select the "Golden Path" using our deterministic random engine
        val goldenPath = allPaths.chooseRandomElement(randomFactory)
        val goldenSet = goldenPath.toSet()

        // Isolate alternative paths
        val alternativePaths = allPaths.filter { it != goldenPath }

        // Compute minimal blockers using the Hitting Set algorithm
        val blockers = if (alternativePaths.isEmpty()) {
            emptyList()
        } else {
            val candidates = alternativePaths.flatten().toSet() - goldenSet - startLocus - endLocus
            findMinimalBlockers(alternativePaths, candidates.toList())
        }

        return@withContext KnightPathExercise(
            board = buildBoard(knightStart = startLocus, destination = endLocus, blockers),
            from = startLocus,
            path = goldenPath,
        )
    }

    private fun findValidExercise(movesRequired: Int): Triple<Locus, Locus, List<List<Locus>>> {
        val capableTargets = Locus.entries.filter { locus ->
            MAX_DISTANCE_MAP[locus]!! >= movesRequired
        }
        // Use a continuous loop instead of a single blind pick.
        // This acts as a self-correcting filter without throwing exceptions.
        while (true) {
            val target = capableTargets.chooseRandomElement(randomFactory)

            // Run a quick distance-only BFS to map out valid starting boundaries
            val shortestPathsMap = bfsAllPathsFrom(target)
            val validStarts = shortestPathsMap.keys.filter { start ->
                val samplePath = shortestPathsMap[start]?.firstOrNull()
                samplePath != null && (samplePath.size - 1) == movesRequired
            }
            if (!validStarts.isEmpty()) {
                val chosenStart = validStarts.chooseRandomElement(randomFactory)

                // Run a clean forward BFS from the actual starting point to the destination target.
                // This guarantees all intermediate paths read naturally from Start -> End
                val forwardPathsMap = bfsAllPathsFrom(chosenStart)
                val forwardPaths = forwardPathsMap[target]!!

                return Triple(chosenStart, target, forwardPaths)
            }
        }
    }

    private fun bfsAllPathsFrom(start: Locus): Map<Locus, List<List<Locus>>> {
        // Standard BFS mapping out ALL shortest paths from a single start point to all reachable points.
        val resultMap = mutableMapOf<Locus, MutableList<List<Locus>>>()
        val queue = LinkedList<List<Locus>>()
        val visited = mutableMapOf<Locus, Int>()

        queue.add(listOf(start))
        visited[start] = 0
        while (queue.isNotEmpty()) {
            val currentPath = queue.poll()
            val currentLocus = currentPath.last()

            resultMap.getOrPut(currentLocus) { mutableListOf() }.add(currentPath)
            Builders.allKnightMoves().mapNotNull { it(currentLocus) }.forEach { nextLocus ->
                val nextDist = currentPath.size
                val existingDist = visited[nextLocus]

                if (existingDist == null || existingDist == nextDist) {
                    visited[nextLocus] = nextDist
                    queue.add(currentPath + nextLocus)
                }
            }
        }
        return resultMap
    }

    private fun findMinimalBlockers(alternativePaths: List<List<Locus>>, candidates: List<Locus>): List<Locus> {
        // Pre-convert each alternative path into a 64-bit mask for instant bitwise evaluation
        val pathMasks = alternativePaths.map { path ->
            var mask = 0L
            for (locus in path) {
                mask = mask or (1L shl locus.ordinal)
            }
            mask
        }

        var bestBlockerCount = candidates.size
        var bestBlockerMask = 0L

        /**
         * @param index The current candidate from candidates
         * @param currentCount How many blockers we have active in the current branch.
         * @param currentMask A 64-bit representation of our active blockers.
         */
        fun backtrack(index: Int, currentCount: Int, currentMask: Long) {
            if (currentCount >= bestBlockerCount) return // Prune branches early

            // Check if ALL path masks share at least one overlapping bit with our current mask
            // This replaces the old .all { .any { ... } } loop with a simple bitwise AND check
            val coversAll = pathMasks.all { pathMask -> (pathMask and currentMask) != 0L }
            if (coversAll) {
                bestBlockerCount = currentCount
                bestBlockerMask = currentMask
                return
            }

            if (index >= candidates.size) return // exhausted all potential candidate squares

            val candidateBit = 1L shl candidates[index].ordinal
            // Option A: Include this candidate square as an active blocker
            backtrack(index + 1, currentCount + 1, currentMask or candidateBit)
            // Option B: Skip this candidate square completely
            backtrack(index + 1, currentCount, currentMask)
        }

        backtrack(index = 0, currentCount = 0, currentMask = 0L) // Initialize recursive search
        // Return the minimal set of blockers from all original candidates
        return candidates.filter { locus ->
            (bestBlockerMask and (1L shl locus.ordinal)) != 0L
        }
    }

    private fun buildBoard(knightStart: Locus, destination: Locus, blockers: List<Locus>): IBoard =
        Builders.boardFactory().emptyBoard().apply {
            add(Piece.Knight, Side.WHITE, knightStart)
            blockers.forEach { add(Piece.Pawn, Side.WHITE, it) }
            add(Piece.Pawn, Side.BLACK, destination)
        }

    companion object {
        private const val MIN_MOVES = 2
        private const val MAX_MOVES = 6

        // A static map of [Locus -> Max Shortest Path Distance achievable from this Locus]
        // This stops us from blindly picking squares that can't support 6 moves
        private val MAX_DISTANCE_MAP: Map<Locus, Int> by lazy {
            Locus.entries.associateWith { locus ->
                val distances = bfsDistancesOnly(locus)
                distances.values.maxOrNull() ?: 0
            }
        }

        // A lightweight distance mapper used solely for initialization and target mapping
        private fun bfsDistancesOnly(start: Locus): Map<Locus, Int> {
            val visited = mutableMapOf<Locus, Int>()
            val queue = LinkedList<Locus>()

            queue.add(start)
            visited[start] = 0

            while (queue.isNotEmpty()) {
                val curr = queue.poll()
                val currentDist = visited[curr]!!

                Builders.allKnightMoves().mapNotNull { it(curr) }.forEach {
                    if (it !in visited) {
                        visited[it] = currentDist + 1
                        queue.add(it)
                    }
                }
            }
            return visited
        }
    }
}

private fun <T> List<T>.chooseRandomElement(factory: RandomFactory): T =
    if (isEmpty()) throw IllegalArgumentException("Can't get a random element from an empty list")
    else this[factory.nextInt(0, this.size)]
