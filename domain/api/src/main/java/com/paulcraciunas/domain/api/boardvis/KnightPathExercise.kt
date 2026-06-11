package com.paulcraciunas.domain.api.boardvis

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus

data class KnightPathExercise(
    val board: IBoard,
    val destination: Locus,
    val path: List<Locus>,
    val movesRequired: Int,
) {
    init {
        assert(path.size == movesRequired + 1)
        assert(path.last() == destination)
    }
}
