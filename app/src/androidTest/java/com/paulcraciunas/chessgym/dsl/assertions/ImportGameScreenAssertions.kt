package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.screens.common.SemanticsKeys
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.tools.importgame.ui.ImportGameScreenTags

class ImportGameScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.SCREEN).assertIsDisplayed()
    }

    fun hasBackgroundColor(color: Color): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.SCREEN).assert(
            SemanticsMatcher.expectValue(SemanticsKeys.BackgroundColor, color)
        )
    }

    fun boardIsLoaded(): ImportGameScreenAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val king = context.getString(R.string.king)
        val pawn = context.getString(R.string.pawn)

        assertPieceAt(File.e, Rank.`1`, king)
        assertPieceAt(File.e, Rank.`8`, king)
        assertPieceAt(File.d, Rank.`2`, pawn)
        assertPieceAt(File.d, Rank.`7`, pawn)
    }

    private fun assertPieceAt(file: File, rank: Rank, pieceDescription: String) {
        rule.onNode(
            hasTestTag(ChessBoardTags.square(file, rank))
                    and hasContentDescription(pieceDescription)
        ).assertIsDisplayed()
    }

    fun showsFenButton(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.FEN_BUTTON).assertIsDisplayed()
    }

    fun showsPgnButton(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.PGN_BUTTON).assertIsDisplayed()
    }

    fun showsDialog(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.IMPORT_DIALOG).assertIsDisplayed()
    }

    fun showsError(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.IMPORT_ERROR).assertIsDisplayed()
    }

    fun confirmButtonIsEnabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.IMPORT_CONFIRM).assertIsEnabled()
    }

    fun confirmButtonIsDisabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.IMPORT_CONFIRM).assertIsNotEnabled()
    }

    fun navigationNextIsDisabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_NEXT).assertIsNotEnabled()
    }

    fun navigationPreviousIsEnabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_PREVIOUS).assertIsEnabled()
    }

    fun navigationPreviousIsDisabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_PREVIOUS).assertIsNotEnabled()
    }

    fun navigationControlsAreVisible(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_NEXT).assertIsDisplayed()
        rule.onNodeWithTag(ImportGameScreenTags.NAV_PREVIOUS).assertIsDisplayed()
        rule.onNodeWithTag(ImportGameScreenTags.NAV_JUMP_TO_START).assertIsDisplayed()
        rule.onNodeWithTag(ImportGameScreenTags.NAV_JUMP_TO_END).assertIsDisplayed()
    }
}
