package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.GenerateRandomLoci
import com.paulcraciunas.domain.api.RandomFactory
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import javax.inject.Inject

/**
 * Implementation of [GenerateRandomLoci] that uses [RandomFactory] to generate
 * random chess square locations.
 */
class GenerateRandomLociImpl @Inject constructor(
    private val randomFactory: RandomFactory
) : GenerateRandomLoci {

    override fun invoke(): Locus {
        val fileIndex = randomFactory.nextInt(from = 0, to = File.entries.size)
        val rankIndex = randomFactory.nextInt(from = 0, to = Rank.entries.size)

        return Locus(
            file = File.fromDec(fileIndex),
            rank = Rank.fromDec(rankIndex)
        )
    }
}
