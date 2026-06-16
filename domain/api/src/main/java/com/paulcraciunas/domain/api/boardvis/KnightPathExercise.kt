package com.paulcraciunas.domain.api.boardvis

import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus

data class KnightPathExercise(
    val board: IBoard,
    val from: Locus,
    val path: List<Locus>, // contains from as first element
) {
    init {
        assert(path.first() == from)
    }
}
