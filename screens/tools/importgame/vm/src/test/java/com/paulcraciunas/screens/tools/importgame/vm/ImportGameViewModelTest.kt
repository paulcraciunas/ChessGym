package com.paulcraciunas.screens.tools.importgame.vm

import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.impl.RealGameFactory
import com.paulcraciunas.serializer.api.Serializer
import com.paulcraciunas.serializer.impl.FenSerializer
import com.paulcraciunas.serializer.impl.PgnSerializer
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ImportGameViewModelTest {
    private val gameFactory = RealGameFactory()
    private val fenSerializer = FenSerializer(gameFactory)
    private val pgnSerializer = PgnSerializer(gameFactory)
    private val appSettingsRepository = FakeAppSettingsRepository()

    private lateinit var underTest: ImportGameViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = ImportGameViewModel(
            fenSerializer = fenSerializer,
            pgnSerializer = pgnSerializer,
            appSettingsRepository = appSettingsRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    internal inner class Initialization {
        @Test
        fun `WHEN viewModel initialized THEN game is not loaded`() = runTest {
            assertFalse(underTest.uiState.value.isGameLoaded)
            assertNull(underTest.uiState.value.importType)
            assertNavigation(back = false, forward = false)
        }
    }

    @Nested
    internal inner class DialogManagement {
        @Test
        fun `GIVEN no dialog WHEN onFenClicked THEN FEN dialog shown`() = runTest {
            underTest.onFenClicked()
            assertEquals(ImportType.FEN, underTest.uiState.value.showImportDialog)
        }

        @Test
        fun `GIVEN no dialog WHEN onPgnClicked THEN PGN dialog shown`() = runTest {
            underTest.onPgnClicked()
            assertEquals(ImportType.PGN, underTest.uiState.value.showImportDialog)
        }

        @Test
        fun `GIVEN FEN dialog shown WHEN onDismissDialog THEN dialog hidden`() = runTest {
            underTest.onFenClicked()
            underTest.onDismissDialog()
            assertNull(underTest.uiState.value.showImportDialog)
        }

        @Test
        fun `GIVEN error shown WHEN dialog dismissed and reopened THEN error cleared`() = runTest {
            underTest.onFenClicked()
            underTest.onImport("invalid")
            assertNotNull(underTest.uiState.value.importError)

            underTest.onDismissDialog()
            underTest.onFenClicked()
            assertNull(underTest.uiState.value.importError)
        }
    }

    @Nested
    internal inner class FenImport {
        @Test
        fun `GIVEN FEN dialog WHEN valid FEN imported THEN game loaded with correct state`() = runTest {
            importStartingPosition()

            val state = underTest.uiState.value
            assertTrue(state.isGameLoaded)
            assertNull(state.showImportDialog)
            assertNull(state.importError)
            assertEquals(ImportType.FEN, state.importType)
            assertNavigation(back = false, forward = false)
        }

        @Test
        fun `GIVEN FEN dialog WHEN invalid FEN imported THEN error shown`() = runTest {
            underTest.onFenClicked()
            underTest.onImport("not a valid fen string")

            val state = underTest.uiState.value
            assertFalse(state.isGameLoaded)
            assertNotNull(state.importError)
            assertEquals(ImportType.FEN, state.showImportDialog)
        }
    }

    @Nested
    internal inner class PgnImport {
        @Test
        fun `GIVEN PGN dialog WHEN valid PGN imported THEN game loaded at last move`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")

            val state = underTest.uiState.value
            assertTrue(state.isGameLoaded)
            assertNull(state.showImportDialog)
            assertEquals(ImportType.PGN, state.importType)
            assertNavigation(back = true, forward = false)
        }

        @Test
        fun `GIVEN PGN with result WHEN imported THEN game loaded at last move`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6 1-0")

            val state = underTest.uiState.value
            assertTrue(state.isGameLoaded)
            assertNavigation(back = true, forward = false)
        }

        @Test
        fun `GIVEN PGN imported WHEN square clicked THEN ignored`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")

            underTest.onSquareClicked(Locus.e2)

            assertNull(underTest.uiState.value.data.boardData.at(Locus.e2).piece?.isSelected)
        }
    }

    @Nested
    internal inner class PgnNavigation {
        @Test
        fun `GIVEN PGN at last move WHEN jump to start THEN shows initial position`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")
            assertNavigation(back = true, forward = false)

            underTest.onJumpToStart()

            assertNavigation(back = false, forward = true)
        }

        @Test
        fun `GIVEN PGN at last move WHEN previous move THEN shows one move back`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")

            underTest.onPreviousMove()

            assertNavigation(back = true, forward = true)
        }

        @Test
        fun `GIVEN PGN at start WHEN next move THEN shows first move`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")
            underTest.onJumpToStart()

            underTest.onNextMove()

            assertNavigation(back = true, forward = true)
        }

        @Test
        fun `GIVEN PGN at middle WHEN jump to end THEN shows last move`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")
            underTest.onJumpToStart()

            underTest.onJumpToEnd()

            assertNavigation(back = true, forward = false)
        }

        @Test
        fun `GIVEN PGN at start WHEN previous move THEN stays at start`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")
            underTest.onJumpToStart()

            underTest.onPreviousMove()

            assertNavigation(back = false, forward = true)
        }

        @Test
        fun `GIVEN PGN at end WHEN next move THEN stays at end`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")

            underTest.onNextMove()

            assertNavigation(back = true, forward = false)
        }

        @Test
        fun `GIVEN PGN navigation WHEN stepping through THEN each position has different board`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")
            underTest.onJumpToStart()

            val boards = mutableListOf(underTest.uiState.value.data.boardData)
            repeat(4) {
                underTest.onNextMove()
                boards.add(underTest.uiState.value.data.boardData)
            }

            assertEquals(5, boards.toSet().size)
        }

        @Test
        fun `GIVEN PGN navigation WHEN navigating THEN clears selection`() = runTest {
            importPgn("1.e4 e5 2.Nf3 Nc6")

            underTest.onPreviousMove()

            assertNull(underTest.uiState.value.data.boardData.at(Locus.e2).piece?.isSelected)
        }
    }

    @Nested
    internal inner class FenPlayAndNavigation {
        @Test
        fun `GIVEN FEN imported WHEN square with piece clicked THEN square selected`() = runTest {
            importStartingPosition()

            underTest.onSquareClicked(Locus.e2)

            assertEquals(true, underTest.uiState.value.data.boardData.at(Locus.e2).piece?.isSelected)
        }

        @Test
        fun `GIVEN piece selected WHEN legal target clicked THEN move made and history updated`() = runTest {
            importStartingPosition()

            underTest.onSquareClicked(Locus.e2)
            underTest.onSquareClicked(Locus.e4)

            val data = underTest.uiState.value.data.boardData
            assertNull(data.at(Locus.e2).piece?.isSelected)
            assertEquals(false, data.at(Locus.e4).piece?.isSelected)
            assertNavigation(back = true, forward = false)
        }

        @Test
        fun `GIVEN multiple moves played WHEN navigate back THEN shows previous position`() = runTest {
            importStartingPosition()
            playMove(Locus.e2, Locus.e4)
            playMove(Locus.e7, Locus.e5)

            underTest.onPreviousMove()

            assertNavigation(back = true, forward = true)
        }

        @Test
        fun `GIVEN navigated back WHEN new move played THEN forward history truncated`() = runTest {
            importStartingPosition()
            playMove(Locus.e2, Locus.e4)
            playMove(Locus.e7, Locus.e5)

            underTest.onJumpToStart()
            playMove(Locus.d2, Locus.d4)

            assertNavigation(back = true, forward = false)
        }

        @Test
        fun `GIVEN FEN with no moves WHEN navigation attempted THEN stays at start`() = runTest {
            importStartingPosition()

            underTest.onPreviousMove()
            assertNavigation(back = false, forward = false)

            underTest.onJumpToStart()
            assertNavigation(back = false, forward = false)
        }

        @Test
        fun `GIVEN FEN navigated back WHEN next and jump to end work THEN navigates forward`() = runTest {
            importStartingPosition()
            playMove(Locus.e2, Locus.e4)
            playMove(Locus.e7, Locus.e5)
            underTest.onJumpToStart()

            underTest.onNextMove()
            assertNavigation(back = true, forward = true)

            underTest.onJumpToEnd()
            assertNavigation(back = true, forward = false)
        }

        @Test
        fun `GIVEN no game loaded WHEN square clicked THEN nothing happens`() = runTest {
            underTest.onSquareClicked(Locus.e4)
            assertNull(underTest.uiState.value.data.boardData.at(Locus.e4).piece?.isSelected)
        }

        @Test
        fun `GIVEN FEN navigated back WHEN playing from history THEN game reconstructed correctly`() = runTest {
            importStartingPosition()
            playMove(Locus.e2, Locus.e4)
            playMove(Locus.e7, Locus.e5)
            playMove(Locus.g1, Locus.f3)

            underTest.onPreviousMove()
            underTest.onPreviousMove()
            assertNavigation(back = true, forward = true)

            playMove(Locus.d7, Locus.d5)

            assertNavigation(back = true, forward = false)
            assertEquals(Side.WHITE, underTest.uiState.value.data.player)
        }
    }

    private fun importStartingPosition() {
        underTest.onFenClicked()
        underTest.onImport(Serializer.STARTING_FEN)
    }

    private fun importPgn(pgn: String) {
        underTest.onPgnClicked()
        underTest.onImport(pgn)
    }

    private fun playMove(from: Locus, to: Locus) {
        underTest.onSquareClicked(from)
        underTest.onSquareClicked(to)
    }

    private fun assertNavigation(back: Boolean, forward: Boolean) {
        assertEquals(back, underTest.uiState.value.canNavigateBack)
        assertEquals(forward, underTest.uiState.value.canNavigateForward)
    }
}
