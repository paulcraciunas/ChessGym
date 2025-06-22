package com.paulcraciunas.puzzles.impl.usecases

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.state.GameInfo
import com.paulcraciunas.puzzles.api.PuzzleRepository
import com.paulcraciunas.puzzles.api.usecases.GetPuzzleSeries
import com.paulcraciunas.puzzles.impl.impl.RandomFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
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
        private val puzzles = mutableMapOf<Int, Puzzle>()

        fun load(count: Int) {
            var rating = GetPuzzleSeries.RATING_START
            for (i in 0 until count) {
                puzzles[rating] = PuzzleStub()
                rating += NEXT_INT
            }
        }

        override suspend fun get(count: Int): List<Puzzle> {
            val result = mutableListOf<Puzzle>()
            puzzles.values.forEachIndexed { index, iPuzzle ->
                if (index < count) {
                    result.add(iPuzzle)
                } else {
                    return@forEachIndexed
                }
            }
            return result
        }

        override suspend fun getByRating(targetRating: Int): Puzzle? = puzzles[targetRating]
        override suspend fun getByRatingRange(min: Int, max: Int): Puzzle? {
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

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
    private class PuzzleStub : Puzzle {
        override val rating: Int = 420
        override val player: Side = Side.WHITE
        override val state: Puzzle.State = Puzzle.State.Idle
        override val info: GameInfo by lazy { throw NotImplementedError() }
        override val board: IBoard  by lazy { throw NotImplementedError() }

        override fun start() {}
        override fun play(ply: Ply) {}
        override fun play(from: Locus, to: Locus) {}
        override fun abandon() {}
        override fun hint(): Piece = Piece.King
    }

    private companion object {
        const val NEXT_INT = 10 // Chosen randomly
    }
}