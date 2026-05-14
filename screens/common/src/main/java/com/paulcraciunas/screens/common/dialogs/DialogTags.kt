package com.paulcraciunas.screens.common.dialogs

import com.paulcraciunas.game.logic.api.board.Piece

/**
 * Test tags for shared dialogs.
 */
object AbandonConfirmationDialogTags {
    const val DIALOG = "abandon_confirmation_dialog"
}

object SignOutDialogTags {
    const val DIALOG = "sign_out_confirmation_dialog"
}

object DeleteAccountDialogTags {
    const val DIALOG = "delete_account_confirmation_dialog"
}

object PromotionDialogTags {
    const val DIALOG = "promotion_dialog"
    private const val CHOICE_PREFIX = "promotion_choice_"

    fun choice(piece: Piece): String = "$CHOICE_PREFIX${piece.name}"
}
