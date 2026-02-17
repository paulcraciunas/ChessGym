package com.paulcraciunas.domain.impl.puzzles

import com.paulcraciunas.domain.api.general.CalculateElo
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.puzzles.GetPuzzleByRating
import com.paulcraciunas.domain.api.puzzles.GetRatedPuzzle
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

// TODO Paul: Unit test me
class GetRatedPuzzleImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val randomFactory: RandomFactory,
    private val getPuzzleByRating: GetPuzzleByRating,
    private val calculateElo: CalculateElo,
) : GetRatedPuzzle {

    override suspend fun invoke(): GetRatedPuzzle.Data {
        val rating = userRepository.get().ratings.current
        val puzzle = getPuzzleByRating(randomFactory.nextInt(rating - GetRatedPuzzle.DEVIATION, rating + GetRatedPuzzle.DEVIATION))
        val eloResult = calculateElo(rating, puzzle.rating)

        return GetRatedPuzzle.Data(
            puzzle = puzzle,
            ratingChange = eloResult,
        )
    }
}
