package com.paulcraciunas.screens.blindmode.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.blindmode.BlindModeGameResult
import com.paulcraciunas.domain.api.blindmode.OnBlindModeGameComplete
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.engine.api.EngineOrchestrator
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.logic.builders.Builders
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.GameSessionFactory
import com.paulcraciunas.screens.data.Outcome
import com.paulcraciunas.screens.data.SessionSettings
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.data.runAs
import com.paulcraciunas.screens.data.toSide
import com.paulcraciunas.screens.data.updateAs
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlindModeViewModel @Inject constructor(
    engineOrchestrator: EngineOrchestrator,
    private val onComplete: OnBlindModeGameComplete,
    private val timer: Timer,
    private val randomFactory: RandomFactory,
    appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val gameFactory: GameFactory = Builders.gameFactory()
    private val factory = GameSessionFactory(engineOrchestrator)
    private val session = factory.get()

    private val _uiState = MutableStateFlow<BlindModeUiState>(BlindModeUiState.Setup())
    val uiState: StateFlow<BlindModeUiState> = _uiState.asStateFlow()

    init {
        session.bindSettings(viewModelScope, appSettingsRepository.appSettings.map {
            SessionSettings(it.autoPromote, it.enableAnimations)
        })
        observeBoardState()
    }

    private fun observeBoardState() {
        viewModelScope.launch {
            session.data.collect { boardState ->
                _uiState.updateAs { it: BlindModeUiState.Playing -> it.copy(data = boardState, moveHistory = session.algebraicHistory()) }
                _uiState.runAs<BlindModeUiState.Playing> {
                    if (boardState.isOver && boardState.interactive) {
                        finishGame(boardState)
                    }
                }
            }
        }
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    fun onTrainingModeToggled(enabled: Boolean) = _uiState.updateAs { it: BlindModeUiState.Setup -> it.copy(isTrainingMode = enabled) }
    fun onSideSelected(side: SideSelection) = _uiState.updateAs { it: BlindModeUiState.Setup -> it.copy(selectedSide = side) }
    fun onPlayClicked() = _uiState.runAs<BlindModeUiState.Setup> { state ->
        val side = state.selectedSide.toSide { randomFactory.nextInt(0, 2) }
        timer.start()
        viewModelScope.launch {
            val elo = userRepository.get().ratings.blindMode.coerceAtLeast(ChessEngine.DEFAULT_ELO)
            val game = gameFactory.builder()
                .withDefaultBoard()
                .withRating(elo)
                .buildGame()
            game.start()
            factory.load(viewModelScope, game, side)
            _uiState.update { BlindModeUiState.Playing(isTrainingMode = state.isTrainingMode, data = session.currentState) }
        }
    }

    fun onSquareClicked(locus: Locus) = _uiState.runAs<BlindModeUiState.Playing> {
        if (!it.isRevealing) session.onClick(locus)
    }

    fun onPromote(to: Piece) = session.promoteIfPending(to)
    fun onResign() = _uiState.runAs<BlindModeUiState.Playing> { session.resign() }
    fun onReveal(): Unit = _uiState.runAs<BlindModeUiState.Playing> { state ->
        if (!state.isRevealAvailable) return
        viewModelScope.launch {
            _uiState.updateAs { it: BlindModeUiState.Playing -> it.copy(isRevealing = true, isRevealAvailable = it.isTrainingMode) }
            delay(REVEAL_DURATION_MS)
            _uiState.updateAs { it: BlindModeUiState.Playing -> it.copy(isRevealing = false) }
        }
    }

    fun onPlayAgain() = _uiState.runAs<BlindModeUiState.GameOver> { state ->
        viewModelScope.launch {
            factory.endSession()
            _uiState.update { BlindModeUiState.Setup(isTrainingMode = state.isTrainingMode) }
        }
    }

    fun onBackPressed(): Boolean {
        _uiState.updateAs { it: BlindModeUiState.Playing -> it.copy(isAbandonDialogShown = true) }
        return _uiState.value is BlindModeUiState.Playing
    }

    fun onAbandonConfirmed() = _uiState.runAs<BlindModeUiState.Playing> { session.resign() }
    fun onAbandonDismissed() = _uiState.updateAs { it: BlindModeUiState.Playing -> it.copy(isAbandonDialogShown = false) }
    private fun finishGame(data: BoardState) {
        viewModelScope.launch {
            onComplete(
                BlindModeGameResult(
                    isPlayerWin = data.outcome == Outcome.Won,
                    movesPlayed = session.completedMoves(),
                    timeSpentMillis = timer.elapsed(),
                    isTrainingMode = _uiState.value.isTrainingMode,
                    opponentElo = data.rating ?: ChessEngine.DEFAULT_ELO,
                )
            )
        }
        _uiState.update {
            BlindModeUiState.GameOver(
                isTrainingMode = it.isTrainingMode,
                data = data,
                moveHistory = session.algebraicHistory(),
            )
        }
    }

    companion object {
        internal const val REVEAL_DURATION_MS = 3000L
    }
}
