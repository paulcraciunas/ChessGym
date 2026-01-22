package com.paulcraciunas.puzzles.api

import com.paulcraciunas.game.logic.api.Puzzle
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.impl.FenSerializer

class FakePuzzleRepository : PuzzleRepository {
    private var id = 0
    private val puzzles = mutableMapOf<Int, Puzzle>()
    private val serializer = FenSerializer(RealGameFactory())

    fun withPuzzle(fen: String, moves: String, rating: Int) {
        puzzles[++id] = serializer.puzzleFrom(puzzleString = fen, moves = moves, rating = rating, id = id)
    }

    fun clear() {
        puzzles.clear()
    }

    override suspend fun get(count: Int): List<Puzzle> = puzzles.values.take(count)
    override suspend fun getById(id: Int): Puzzle? = puzzles[id]
    override suspend fun getByRating(targetRating: Int): Puzzle? =
        puzzles.values.firstOrNull { it.rating == targetRating }

    override suspend fun getByRatingRange(min: Int, max: Int): Puzzle? =
        puzzles.values.firstOrNull { it.rating in min..max }

    companion object {
        const val SIZE = 20
        fun default(ratingStart: Int = 1200, increment: Int = 50): FakePuzzleRepository = FakePuzzleRepository().apply {
            val ratings = IntArray(size = SIZE) { i -> ratingStart + i * increment }
            withPuzzle("2k3r1/pppn1p2/8/3p2N1/1P1P1q2/2P1R3/P4PPN/R5K1 b - - 0 21", "g8g5 e3e8", ratings[0])
            withPuzzle("7k/1Q1q2pp/p4r2/5p2/3p1P2/3P4/6PP/4R2K b - - 0 31", "d7b7 e1e8 f6f8 e8f8", ratings[1])
            withPuzzle("6k1/6r1/1R3K2/8/8/8/8/8 b - - 8 76", "g8f8 b6b8", ratings[2])
            withPuzzle("5rkR/3r2q1/3p2P1/2pPnp1Q/p1P1B3/Pp1P4/1P4K1/7R b - - 1 38", "g7h8 h5h8", ratings[3])
            withPuzzle("4r3/1k6/1pn5/2p2p1p/2P4P/3P2p1/2QB2P1/4q2K w - - 2 47", "d2e1 e8e1", ratings[4])
            withPuzzle("r5k1/1ppbq3/5p1p/1Q6/p2P4/P7/1P3PPP/R1B3K1 w - - 2 34", "b5b7 e7e1", ratings[5])
            withPuzzle("b2RQ2k/p5pp/1n2r3/1P2Pp2/8/P7/B4PP1/6K1 b - - 0 32", "e6e8 d8e8", ratings[6])
            withPuzzle("r3R2k/6p1/p6p/8/3r4/3BR2P/PP4P1/6K1 b - - 1 33", "a8e8 e3e8", ratings[7])
            withPuzzle("5Bk1/p2R1pbp/6p1/8/8/8/2P2PPP/r2Qq1K1 w - - 0 22", "d1e1 a1e1", ratings[8])
            withPuzzle("2r4k/ppBb1Bp1/3P2Qp/1q6/8/8/PP4PP/5R1K w - - 3 32", "f7b3 b5f1", ratings[9])
            withPuzzle("rn1Q3k/6pp/ppp2b2/8/P6q/2P5/BP3PPP/R1B1R1K1 b - - 0 18", "f6d8 e1e8", ratings[10])
            withPuzzle("2rr2k1/p4ppp/2p2b2/4pP2/2p5/6RQ/PP4PP/1R1q3K w - - 2 25", "b1d1 d8d1", ratings[11])
            withPuzzle("2kr3r/ppp2ppp/3b4/4q3/3pPN2/3P3P/PPPQ1PP1/R4RK1 w - - 4 16", "f4e2 e5h2", ratings[12])
            withPuzzle("r4rk1/5p1p/1pb1qBp1/1ppp4/3P3Q/P1P2N2/5PPP/RN4K1 w - - 1 19", "f3g5 e6e1", ratings[13])
            withPuzzle("1r4k1/5pbp/4pBp1/4P3/7N/8/6PP/1q3Q1K w - - 0 37", "f1b1 b8b1", ratings[14])
            withPuzzle("r2q1rk1/1b2bpp1/2p1p2p/1pPp4/1P4Q1/P3P2P/1B1N1PP1/R3R1K1 b - - 0 18", "b7c8 g4g7", ratings[15])
            withPuzzle("6rk/5p2/4pP2/1p1pP1rp/p7/P1P4R/1P3n1P/3B1R1K w - - 4 30", "f1f2 g5g1", ratings[16])
            withPuzzle("r4k1r/ppp1BBpp/4Qb2/8/3q4/8/P4PPP/4R1K1 b - - 0 18", "f6e7 e6e7", ratings[17])
            withPuzzle("3r1b1r/p3k1pp/2Q2p2/4p3/4n3/2P5/PP4PP/RNB3qK w - - 0 17", "h1g1 d8d1", ratings[18])
            withPuzzle("3r1qk1/ppp3R1/2n1P2p/8/2P1p3/1PQ5/PB3PPP/6K1 b - - 0 22", "f8g7 c3g7", ratings[19])
        }
    }
}
