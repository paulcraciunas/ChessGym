package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.CalculateElo
import com.paulcraciunas.domain.api.GetPuzzleByRating
import com.paulcraciunas.domain.api.GetRatedPuzzle
import com.paulcraciunas.user.api.UserRepository
import javax.inject.Inject

class GetRatedPuzzleImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val getPuzzleByRating: GetPuzzleByRating,
    private val calculateElo: CalculateElo,
) : GetRatedPuzzle {

    override suspend fun invoke(): GetRatedPuzzle.Data {
        val rating = userRepository.get().ratings.current
        val puzzle = getPuzzleByRating(rating)
        val eloResult = calculateElo(rating, puzzle.rating)

        return GetRatedPuzzle.Data(
            puzzle = puzzle,
            ratingChange = eloResult,
        )
    }
}
