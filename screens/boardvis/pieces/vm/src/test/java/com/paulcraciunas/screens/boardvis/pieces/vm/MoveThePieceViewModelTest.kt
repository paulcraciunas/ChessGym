package com.paulcraciunas.screens.boardvis.pieces.vm

import com.paulcraciunas.domain.api.general.FakeCountdownTimer
import com.paulcraciunas.domain.api.GameEngineState
import com.paulcraciunas.domain.api.MoveResult
import com.paulcraciunas.domain.api.MoveThePieceGameEngine
import com.paulcraciunas.domain.api.MoveThePieceResult
import com.paulcraciunas.domain.api.OnMoveThePieceComplete
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.api.board.Rank
import com.paulcraciunas.game.logic.impl.board.Board
import com.paulcraciunas.user.api.FakeUserRepository
import com.paulcraciunas.user.api.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class MoveThePieceViewModelTest {
    private val userRepository = FakeUserRepository()
    private val fakeCountdownTimer = FakeCountdownTimer()
    private val fakeGameEngine = FakeGameEngine()
    private val fakeOnComplete = FakeOnMoveThePieceComplete()

    private lateinit var underTest: MoveThePieceViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN initial state WHEN viewModel created THEN uiState is Setup`() = runTest {
        // Given/When
        setupViewModel()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Setup)
        assertTrue((uiState as MoveThePieceUiState.Setup).isTrainingMode)
        assertEquals(Piece.Rook, uiState.selectedPiece)
    }

    @Test
    fun `GIVEN setup state WHEN onTrainingModeToggled to false THEN isTrainingMode updated`() = runTest {
        // Given
        setupViewModel()

        // When
        underTest.onTrainingModeToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Setup)
        assertFalse((uiState as MoveThePieceUiState.Setup).isTrainingMode)
    }

    @Test
    fun `GIVEN setup state WHEN onPieceSelected THEN selectedPiece updated`() = runTest {
        // Given
        setupViewModel()

        // When
        underTest.onPieceSelected(Piece.Knight)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Setup)
        assertEquals(Piece.Knight, (uiState as MoveThePieceUiState.Setup).selectedPiece)
    }

    @Test
    fun `GIVEN setup state with training mode WHEN onPlayClicked THEN game starts with selected piece`() = runTest {
        // Given
        setupViewModel()
        underTest.onPieceSelected(Piece.Bishop)
        testDispatcher.scheduler.advanceUntilIdle()

        fakeGameEngine.currentState = createGameState(playerPiece = Piece.Bishop)

        // When
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Playing)
        assertEquals(Piece.Bishop, (uiState as MoveThePieceUiState.Playing).playerPiece)
        assertTrue(fakeCountdownTimer.isRunning)
        assertTrue(fakeGameEngine.gameStarted)
    }

    @Test
    fun `GIVEN setup state without training mode WHEN onPlayClicked THEN game starts with Bishop`() = runTest {
        // Given
        setupViewModel()
        underTest.onTrainingModeToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        fakeGameEngine.currentState = createGameState(playerPiece = Piece.Bishop)

        // When
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(Piece.Bishop, fakeGameEngine.lastStartPiece)
    }

    @Test
    fun `GIVEN playing state WHEN valid move made THEN state updated`() = runTest {
        // Given
        setupViewModel()
        fakeGameEngine.currentState = createGameState()
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val newState = createGameState(
            playerPieceLocus = Locus(File.e, Rank.`4`),
            movesRemaining = 1
        )
        fakeGameEngine.moveResult = MoveResult.Success(newState)

        // When
        underTest.onSquareClicked(Locus(File.e, Rank.`4`))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Playing)
        assertEquals(Locus(File.e, Rank.`4`), (uiState as MoveThePieceUiState.Playing).playerPieceLocus)
        assertEquals(1, uiState.movesRemaining)
    }

    @Test
    fun `GIVEN playing state WHEN invalid move made THEN state unchanged`() = runTest {
        // Given
        setupViewModel()
        fakeGameEngine.currentState = createGameState()
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeGameEngine.moveResult = MoveResult.Invalid

        // When
        underTest.onSquareClicked(Locus(File.a, Rank.`1`))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Playing)
        // State should be unchanged
        assertEquals(Locus(File.d, Rank.`4`), (uiState as MoveThePieceUiState.Playing).playerPieceLocus)
    }

    @Test
    fun `GIVEN playing state WHEN captured THEN game ends`() = runTest {
        // Given
        setupViewModel()
        fakeGameEngine.currentState = createGameState()
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        fakeGameEngine.moveResult = MoveResult.Captured

        // When
        underTest.onSquareClicked(Locus(File.d, Rank.`2`))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.GameOver)
        assertTrue((uiState as MoveThePieceUiState.GameOver).wasCaptured)
        assertFalse(fakeCountdownTimer.isRunning)
        assertTrue(fakeOnComplete.invoked)
    }

    @Test
    fun `GIVEN playing state WHEN level completed THEN state updated with new level`() = runTest {
        // Given
        setupViewModel()
        fakeGameEngine.currentState = createGameState()
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val newState = createGameState(
            currentScore = 1,
            level = 2,
            movesRemaining = 1
        )
        fakeGameEngine.moveResult = MoveResult.LevelComplete(newState)

        // When
        underTest.onSquareClicked(Locus(File.e, Rank.`4`))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Playing)
        assertEquals(1, (uiState as MoveThePieceUiState.Playing).currentScore)
    }

    @Test
    fun `GIVEN playing state WHEN timer expires THEN game ends`() = runTest {
        // Given
        setupViewModel()
        fakeGameEngine.currentState = createGameState(currentScore = 3)
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When - simulate timer expiry
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.GameOver)
        assertFalse((uiState as MoveThePieceUiState.GameOver).wasCaptured)
        assertEquals(3, uiState.finalScore)
        assertTrue(fakeOnComplete.invoked)
    }

    @Test
    fun `GIVEN non-training mode with score above high score WHEN game ends THEN isNewHighScore true`() = runTest {
        // Given
        setupViewModel(User(highScores = User.HighScores(moveThePiece = 5)))
        underTest.onTrainingModeToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        fakeGameEngine.currentState = createGameState(
            currentScore = 10,
            isTrainingMode = false
        )
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.GameOver)
        assertTrue((uiState as MoveThePieceUiState.GameOver).isNewHighScore)
    }

    @Test
    fun `GIVEN training mode with score above high score WHEN game ends THEN isNewHighScore false`() = runTest {
        // Given
        setupViewModel(User(highScores = User.HighScores(moveThePiece = 5)))

        fakeGameEngine.currentState = createGameState(
            currentScore = 10,
            isTrainingMode = true
        )
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.GameOver)
        assertFalse((uiState as MoveThePieceUiState.GameOver).isNewHighScore)
    }

    @Test
    fun `GIVEN game over WHEN onPlayAgain called THEN returns to setup state`() = runTest {
        // Given
        setupViewModel()
        fakeGameEngine.currentState = createGameState()
        underTest.onPlayClicked()
        testDispatcher.scheduler.advanceUntilIdle()
        fakeCountdownTimer.advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        underTest.onPlayAgain()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = underTest.uiState.value
        assertTrue(uiState is MoveThePieceUiState.Setup)
        assertTrue(fakeGameEngine.resetCalled)
    }

    private fun setupViewModel(user: User = User()) = runTest {
        userRepository.local.saveUser(user)
        underTest = MoveThePieceViewModel(
            gameEngine = fakeGameEngine,
            onComplete = fakeOnComplete,
            countdownTimer = fakeCountdownTimer,
            userRepository = userRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun createGameState(
        playerPiece: Piece = Piece.Rook,
        playerPieceLocus: Locus = Locus(File.d, Rank.`4`),
        board: IBoard = Board(),
        opposingPieces: Map<Locus, Piece> = emptyMap(),
        visitedSquares: Set<Locus> = setOf(playerPieceLocus),
        movesRemaining: Int = 2,
        currentScore: Int = 0,
        level: Int = 1,
        isTrainingMode: Boolean = true,
        isGameOver: Boolean = false,
        wasCaptured: Boolean = false
    ) = GameEngineState(
        playerPiece = playerPiece,
        playerPieceLocus = playerPieceLocus,
        opposingPieces = opposingPieces,
        board = board,
        visitedSquares = visitedSquares,
        movesRemaining = movesRemaining,
        currentScore = currentScore,
        level = level,
        isTrainingMode = isTrainingMode,
        isGameOver = isGameOver,
        wasCaptured = wasCaptured
    )
}

private class FakeGameEngine : MoveThePieceGameEngine {
    var currentState: GameEngineState? = null
    var moveResult: MoveResult = MoveResult.Invalid
    var gameStarted = false
        private set
    var resetCalled = false
        private set
    var lastStartPiece: Piece? = null
        private set

    override fun startGame(
        piece: Piece,
        requiredMoves: Int,
        opposingPieceCount: Int,
        isTrainingMode: Boolean,
    ) {
        gameStarted = true
        lastStartPiece = piece
    }

    override fun getState(): GameEngineState {
        return currentState ?: throw IllegalStateException("Game not started")
    }

    override fun makeMove(to: Locus): MoveResult {
        return moveResult
    }

    override fun reset() {
        resetCalled = true
        currentState = null
    }
}

private class FakeOnMoveThePieceComplete : OnMoveThePieceComplete {
    var invoked = false
        private set
    var lastResult: MoveThePieceResult? = null
        private set

    override suspend fun invoke(result: MoveThePieceResult) {
        invoked = true
        lastResult = result
    }
}
