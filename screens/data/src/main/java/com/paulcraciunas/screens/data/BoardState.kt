package com.paulcraciunas.screens.data

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus

enum class Outcome {
    Won,
    Drew,
    Lost,
}

@Immutable
data class BoardState(
    val rating: Int?,
    val player: Side,
    val id: Int?,
    val boardData: BoardViewData,
    val promotion: Promotion?,
    val movePlayed: Boolean,
    val captured: CapturedPieces,
    val outcome: Outcome?,
) {
    val won: Boolean // simple convenience property, as we query this in multiple places
        get() = outcome == Outcome.Won

    companion object {
        val empty = BoardState(
            player = Side.WHITE,
            rating = null,
            id = null,
            boardData = BoardViewData.empty(),
            promotion = null,
            movePlayed = false,
            captured = CapturedPieces("", ""),
            outcome = null,
        )
    }
}

@Immutable
data class CapturedPieces(
    val byPlayer: String,
    val byOpponent: String,
)

@Immutable
data class Promotion(
    val showChooser: Boolean,
    val at: Locus,
)
