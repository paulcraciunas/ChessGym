package com.paulcraciunas.screens.common.board

import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Rank

/**
 * Test tags for the shared [ChessBoard] composable.
 *
 * Every playable square gets a stable, algebraic-notation based tag (e.g. `square_e4`) so that
 * UI tests can deterministically target any square regardless of board orientation.
 */
object ChessBoardTags {
    private const val SQUARE_PREFIX = "square_"
    const val BORDER = "board_border"

    fun square(file: File, rank: Rank): String = "$SQUARE_PREFIX${file.name}${rank.name}"
}
