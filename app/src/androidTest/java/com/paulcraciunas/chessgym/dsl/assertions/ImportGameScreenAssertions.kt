package com.paulcraciunas.chessgym.dsl.assertions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.global.resources.R
import com.paulcraciunas.screens.common.board.ChessBoardTags
import com.paulcraciunas.screens.common.controls.MoveNavigationTags
import com.paulcraciunas.screens.tools.importgame.ui.ImportGameScreenTags

class ImportGameScreenAssertions(private val rule: ComposeTestRule) {

    fun isDisplayed(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.SCREEN).assertIsDisplayed()
    }

    fun boardIsLoaded(): ImportGameScreenAssertions = apply {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val king = context.getString(R.string.king)
        val pawn = context.getString(R.string.pawn)

        assertPieceAt(Locus.e1, king)
        assertPieceAt(Locus.e8, king)
        assertPieceAt(Locus.d2, pawn)
        assertPieceAt(Locus.d7, pawn)
    }

    private fun assertPieceAt(at: Locus, pieceDescription: String) {
        rule.onNode(
            hasTestTag(ChessBoardTags.square(at))
                and hasContentDescription(pieceDescription)
        ).assertIsDisplayed()
    }

    fun showsImportButton(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.PGN_BUTTON).assertIsDisplayed()
    }

    fun showsError(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.IMPORT_ERROR).assertIsDisplayed()
    }

    fun importButtonIsEnabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.PGN_BUTTON).assertIsEnabled()
    }

    fun importButtonIsDisabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.PGN_BUTTON).assertIsNotEnabled()
    }

    fun navigationNextIsDisabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(MoveNavigationTags.NEXT).assertIsNotEnabled()
    }

    fun navigationPreviousIsEnabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(MoveNavigationTags.PREVIOUS).assertIsEnabled()
    }

    fun navigationPreviousIsDisabled(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(MoveNavigationTags.PREVIOUS).assertIsNotEnabled()
    }

    fun navigationControlsAreVisible(): ImportGameScreenAssertions = apply {
        rule.onNodeWithTag(MoveNavigationTags.NEXT).assertIsDisplayed()
        rule.onNodeWithTag(MoveNavigationTags.PREVIOUS).assertIsDisplayed()
        rule.onNodeWithTag(MoveNavigationTags.JUMP_TO_START).assertIsDisplayed()
        rule.onNodeWithTag(MoveNavigationTags.JUMP_TO_END).assertIsDisplayed()
    }
}
