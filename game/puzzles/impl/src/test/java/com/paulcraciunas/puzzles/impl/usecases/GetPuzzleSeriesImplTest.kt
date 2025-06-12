package com.paulcraciunas.puzzles.impl.usecases

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.IPuzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.board.BoardFactory
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.puzzles.api.usecases.GetPuzzleSeries
import com.paulcraciunas.puzzles.impl.impl.RandomFactory
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class GetPuzzleSeriesImplTest {
    private val randomGen = FakeRandom()
    private val repository = FakeRepository()

    private val underTest = GetPuzzleSeriesImpl(repository, randomGen)

    @Test
    fun `WHEN puzzle repository is empty THEN no puzzles are returned`() = runBlocking {
        val puzzles = underTest()

        assertEquals(0, puzzles.size)
    }

    @Test
    fun `WHEN puzzle repository does not contain desired puzzle THEN remaining puzzles are returned`() = runBlocking {
        repository.load(10)

        val puzzles = underTest()

        // then
        assertEquals(10, puzzles.size)
    }

    @Test
    fun `WHEN puzzle repository contains puzzles THEN return them`() = runBlocking {
        repository.load(GetPuzzleSeries.COUNT)

        val puzzles = underTest()

        assertEquals(GetPuzzleSeries.COUNT, puzzles.size)
    }

    private class FakeRepository : PuzzleRepository {
        private val puzzles = mutableMapOf<Int, IPuzzle>()

        fun load(count: Int) {
            var rating = GetPuzzleSeries.RATING_START
            for (i in 0 until count) {
                puzzles[rating] = PuzzleStub()
                rating += NEXT_INT
            }
        }

        override suspend fun get(count: Int): List<IPuzzle> {
            val result = mutableListOf<IPuzzle>()
            puzzles.values.forEachIndexed { index, iPuzzle ->
                if (index < count) {
                    result.add(iPuzzle)
                } else {
                    return@forEachIndexed
                }
            }
            return result
        }

        override suspend fun getByRating(targetRating: Int): IPuzzle? = puzzles[targetRating]
        override suspend fun getByRatingRange(min: Int, max: Int): IPuzzle? {
            for (i in min until max) {
                if (puzzles.contains(i)) {
                    return puzzles[i]
                }
            }
            return null
        }
    }

    private class FakeRandom : RandomFactory {
        override fun nextInt(from: Int, to: Int): Int = NEXT_INT
    }

    private class PuzzleStub : IPuzzle {
        override fun turn(): Side = Side.WHITE
        override fun board(): IBoard = BoardFactory.defaultBoard()
        override fun isOver(): IPuzzle.Result? = null
        override fun playablePlies(from: Locus): Collection<Ply> = emptyList()
        override fun play(ply: Ply) {}
        override fun promote(piece: Piece, on: Ply) {}
        override fun resign() {}
    }

    private companion object {
        const val NEXT_INT = 10 // Chosen randomly
    }
}