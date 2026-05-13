package com.paulcraciunas.chessgym.screens.tools

import com.paulcraciunas.chessgym.base.BaseUiTest
import com.paulcraciunas.chessgym.dsl.Given
import com.paulcraciunas.chessgym.dsl.Then
import com.paulcraciunas.chessgym.dsl.When
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
internal class ImportGameScreenTest : BaseUiTest() {

    @Before
    override fun setUp() {
        super.setUp()
        Given.settings.puzzlesDownloaded()
        Given.user.isDefault()

        When.appIsLaunched()
        When.navigation.navigateToTools()
        When.toolsDashboard.openImportGame()
    }

    @Test
    fun WHEN_navigated_to_import_game_THEN_shows_import_buttons() {
        Then.importGame
            .isDisplayed()
            .showsFenButton()
            .showsPgnButton()
    }

    @Test
    fun WHEN_tapping_fen_button_THEN_dialog_opens() {
        When.importGame.openFenDialog()

        Then.importGame.showsDialog()
    }

    @Test
    fun WHEN_tapping_pgn_button_THEN_dialog_opens() {
        When.importGame.openPgnDialog()

        Then.importGame.showsDialog()
    }

    @Test
    fun GIVEN_empty_dialog_WHEN_opened_THEN_confirm_is_disabled() {
        When.importGame.openFenDialog()

        Then.importGame
            .showsDialog()
            .confirmButtonIsDisabled()
    }

    @Test
    fun GIVEN_text_in_dialog_WHEN_typed_THEN_confirm_is_enabled() {
        When.importGame.openFenDialog()
            .typeInDialog(STARTING_FEN)

        Then.importGame.confirmButtonIsEnabled()
    }

    @Test
    fun GIVEN_valid_fen_WHEN_imported_THEN_board_is_loaded() {
        When.importGame.openFenDialog()
            .typeInDialog(STARTING_FEN)
            .confirmImport()

        Then.importGame.boardIsLoaded()
    }

    @Test
    fun GIVEN_invalid_fen_WHEN_imported_THEN_error_is_shown() {
        When.importGame.openFenDialog()
            .typeInDialog(INVALID_FEN)
            .confirmImport()

        Then.importGame
            .showsDialog()
            .showsError()
    }

    @Test
    fun GIVEN_valid_pgn_WHEN_imported_THEN_board_is_loaded_with_navigation() {
        When.importGame.openPgnDialog()
            .typeInDialog(SAMPLE_PGN)
            .confirmImport()

        Then.importGame
            .boardIsLoaded()
            .navigationControlsAreVisible()
    }

    @Test
    fun GIVEN_pgn_loaded_at_start_WHEN_next_move_THEN_advances_position() {
        When.importGame.openPgnDialog()
            .typeInDialog(SAMPLE_PGN)
            .confirmImport()
            .nextMove()

        Then.importGame
            .isDisplayed()
            .navigationPreviousIsEnabled()
    }

    @Test
    fun GIVEN_pgn_loaded_at_end_WHEN_previous_move_THEN_previous_position() {
        When.importGame.openPgnDialog()
            .typeInDialog(SAMPLE_PGN)
            .confirmImport()
            .previousMove()

        Then.importGame.navigationPreviousIsEnabled()
    }

    @Test
    fun GIVEN_pgn_loaded_WHEN_jump_to_end_THEN_at_last_position() {
        When.importGame.openPgnDialog()
            .typeInDialog(SAMPLE_PGN)
            .confirmImport()
            .jumpToEnd()

        Then.importGame.navigationNextIsDisabled()
    }

    @Test
    fun GIVEN_pgn_at_end_WHEN_jump_to_start_THEN_at_first_position() {
        When.importGame.openPgnDialog()
            .typeInDialog(SAMPLE_PGN)
            .confirmImport()
            .jumpToEnd()
            .jumpToStart()

        Then.importGame.navigationPreviousIsDisabled()
    }

    @Test
    fun GIVEN_light_mode_WHEN_navigated_to_import_game_THEN_shows_light_background() {
        Given.settings.lightMode()

        Then.importGame.isDisplayed()
        Then.theme.isLightMode()
    }

    @Test
    fun GIVEN_dark_mode_WHEN_navigated_to_import_game_THEN_shows_dark_background() {
        Given.settings.darkMode()

        Then.importGame.isDisplayed()
        Then.theme.isDarkMode()
    }

    @Test
    fun WHEN_going_back_THEN_returns_to_tools_dashboard() {
        Then.importGame.isDisplayed()

        When.navigation.goBack()

        Then.toolsDashboard.isDisplayed()
    }

    private companion object {
        const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        const val INVALID_FEN = "not-a-valid-fen-string"
        const val SAMPLE_PGN = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6"
    }
}
