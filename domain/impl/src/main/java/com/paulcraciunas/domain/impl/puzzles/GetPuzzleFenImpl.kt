package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.puzzles.GetPuzzleFen
import com.paulcraciunas.domain.api.puzzles.PuzzleAnalysisData
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.serializer.api.Serializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetPuzzleFenImpl(
    private val puzzleRepository: PuzzleRepository,
    private val fenSerializer: Serializer,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GetPuzzleFen {

    override suspend fun invoke(puzzleId: Int): PuzzleAnalysisData? = withContext(ioDispatcher) {
        val puzzle = puzzleRepository.getById(puzzleId) ?: return@withContext null
        puzzle.start()
        val firstMove = puzzle.expectedMoves.firstOrNull() ?: return@withContext null
        val fen = fenSerializer.of(puzzle)
        return@withContext PuzzleAnalysisData(fen = fen, firstMove = firstMove)
    }
}
