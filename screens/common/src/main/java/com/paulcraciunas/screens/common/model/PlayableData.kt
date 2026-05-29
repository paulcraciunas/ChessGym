package com.paulcraciunas.screens.common.model

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus

@Immutable
data class PlayableData(
    val rating: Int?,
    val player: Side,
    val id: Int?,
    val boardData: BoardViewData,
    val captured: CapturedPieces,
    val isOver: Boolean,
    val outcome: Outcome?,
) {
    val won: Boolean // simple convenience property, as we query this in multiple places
        get() = outcome == Outcome.Won

    enum class Outcome {
        Won,
        Drew,
        Lost,
    }
}

@Immutable
data class CapturedPieces(
    val byPlayer: String,
    val byOpponent: String,
)

@Immutable
data class ClickResult(
    val data: PlayableData,
    val promotion: Promotion?,
    val movePlayed: Boolean,

)

@Immutable
data class Promotion(
    val showChooser: Boolean,
    val at: Locus,
)
