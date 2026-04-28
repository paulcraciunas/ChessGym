package com.paulcraciunas.chessgym.dsl.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.paulcraciunas.screens.tools.importgame.ui.ImportGameScreenTags

class ImportGameScreenActions(private val rule: ComposeTestRule) {

    fun openFenDialog(): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.FEN_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun openPgnDialog(): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.PGN_BUTTON).performClick()
        rule.waitForIdle()
    }

    fun typeInDialog(text: String): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.IMPORT_TEXT_FIELD).performTextInput(text)
        rule.waitForIdle()
    }

    fun confirmImport(): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.IMPORT_CONFIRM).performClick()
        rule.waitForIdle()
    }

    fun nextMove(): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_NEXT).performClick()
        rule.waitForIdle()
    }

    fun previousMove(): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_PREVIOUS).performClick()
        rule.waitForIdle()
    }

    fun jumpToStart(): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_JUMP_TO_START).performClick()
        rule.waitForIdle()
    }

    fun jumpToEnd(): ImportGameScreenActions = apply {
        rule.onNodeWithTag(ImportGameScreenTags.NAV_JUMP_TO_END).performClick()
        rule.waitForIdle()
    }
}
