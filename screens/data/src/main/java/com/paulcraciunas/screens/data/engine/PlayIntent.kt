package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

sealed class PlayIntent(internal val blockedByAnimation: Boolean) {
    data class SelectSquare(val selection: Locus) : PlayIntent(blockedByAnimation = true)
    data class Promote(val to: Piece) : PlayIntent(blockedByAnimation = true)
    data class Navigate(val type: Navigation) : PlayIntent(blockedByAnimation = true)
    data object Hint : PlayIntent(blockedByAnimation = true)
    data object RequestAbandon : PlayIntent(blockedByAnimation = true)
    data object DismissAbandon : PlayIntent(blockedByAnimation = true)
    data object ConfirmAbandon : PlayIntent(blockedByAnimation = true)
    data object Resume : PlayIntent(blockedByAnimation = true)

    internal data class AnimationPhaseComplete(
        val phase: AnimationPhase,
    ) : PlayIntent(blockedByAnimation = false)

    internal data object SessionFinished : PlayIntent(blockedByAnimation = false)
    internal data object SessionFailed : PlayIntent(blockedByAnimation = false)

    enum class Navigation {
        ToStart,
        Back,
        Forward,
        ToEnd,
    }

    internal enum class AnimationPhase {
        MoveAnimated,
        OpponentMoveAnimated,
        BoardSwapped,
        SolutionStepAnimated,
    }
}
