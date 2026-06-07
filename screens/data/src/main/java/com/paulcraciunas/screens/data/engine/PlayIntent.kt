package com.paulcraciunas.screens.data.engine

import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

sealed class PlayIntent(
    internal val blockedByAnimation: Boolean,
    internal val resumable: Boolean,
) {
    data class SelectSquare(val selection: Locus) : PlayIntent(blockedByAnimation = true, resumable = false)
    data class Promote(val to: Piece) : PlayIntent(blockedByAnimation = true, resumable = false)
    data class Navigate(val type: Navigation) : PlayIntent(blockedByAnimation = true, resumable = false)
    data object Hint : PlayIntent(blockedByAnimation = true, resumable = false)
    data object RequestAbandon : PlayIntent(blockedByAnimation = true, resumable = false)
    data object DismissAbandon : PlayIntent(blockedByAnimation = true, resumable = false)
    data object ConfirmAbandon : PlayIntent(blockedByAnimation = true, resumable = false)
    data object Resume : PlayIntent(blockedByAnimation = true, resumable = true)

    // Implementation details
    internal data object ExpireTime : PlayIntent(blockedByAnimation = false, resumable = true)
    internal data object SessionFinished : PlayIntent(blockedByAnimation = false, resumable = false)
    internal data object SessionFailed : PlayIntent(blockedByAnimation = false, resumable = false)

    enum class Navigation {
        ToStart,
        Back,
        Forward,
        ToEnd,
    }
}
