package com.paulcraciunas.screens.blindmode.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paulcraciunas.domain.api.blindmode.BlindModeGameResult
import com.paulcraciunas.domain.api.blindmode.BlindModeOrchestrator
import com.paulcraciunas.domain.api.blindmode.OnBlindModeGameComplete
import com.paulcraciunas.domain.api.general.RandomFactory
import com.paulcraciunas.domain.api.general.Timer
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.data.SideSelection
import com.paulcraciunas.screens.data.BoardInteractionHelper
import com.paulcraciunas.screens.data.ClickResult
import com.paulcraciunas.screens.data.GameNavigation
import com.paulcraciunas.screens.data.GamePlayableBoard
import com.paulcraciunas.screens.data.PlayableData
import com.paulcraciunas.settings.application.api.AppSettingsRepository
import com.paulcraciunas.user.api.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlindModeViewModel @Inject constructor(
    private val orchestrator: BlindModeOrchestrator,
    private val onComplete: OnBlindModeGameComplete,
    private val timer: Timer,
    private val randomFactory: RandomFactory,
    private val appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val helper = BoardInteractionHelper(navigation = GameNavigation())

    private val _uiState = MutableStateFlow<BlindModeUiState>(BlindModeUiState.Setup())
    val uiState: StateFlow<BlindModeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.appSettings.collect { settings ->
                helper.autoPromote = settings.autoPromote
            }
        }
    }

    fun onStop() {
        timer.pause()
    }

    fun onStart() {
        timer.resume()
    }

    fun onTrainingModeToggled(enabled: Boolean) = _uiState.updateAs<BlindModeUiState.Setup> { it.copy(isTrainingMode = enabled) }
    fun onSideSelected(side: SideSelection) = _uiState.updateAs<BlindModeUiState.Setup> { it.copy(selectedSide = side) }
    fun onPlayClicked() = runAs<BlindModeUiState.Setup> { state ->
        val side = when (state.selectedSide) {
            SideSelection.WHITE -> Side.WHITE
            SideSelection.BLACK -> Side.BLACK
            SideSelection.RANDOM -> Side.fromCode(randomFactory.nextInt(0, 2))
        }
        val isTrainingMode = state.isTrainingMode
        timer.start()
        viewModelScope.launch {
            val elo = userRepository.get().ratings.blindMode.coerceAtLeast(ChessEngine.DEFAULT_ELO)
            val game = orchestrator.startGame(elo = elo, side = side)

            _uiState.update { BlindModeUiState.Playing(isTrainingMode = isTrainingMode, data = helper.load(GamePlayableBoard(game, side))) }
            if (side == Side.BLACK) {
                requestEngineMove()
            }
        }
    }

    fun onSquareClicked(locus: Locus): Unit = playInteractive {
        handleMoveResult(helper.handleSquareClick(locus))
    }

    fun onPromote(to: Piece): Unit = playInteractive { state ->
        state.pendingPromotion?.let {
            handleMoveResult(helper.promote(to, state.pendingPromotion.at))
        }
    }

    private fun handleMoveResult(result: ClickResult) {
        _uiState.updateAs<BlindModeUiState.Playing> {
            it.copy(data = result.data, pendingPromotion = result.promotion, moveHistory = helper.algebraicHistory())
        }
        if (result.data.isOver) {
            finishGame(data = result.data)
        } else if (result.movePlayed) {
            requestEngineMove()
        }
    }

    fun onResign() = runAs<BlindModeUiState.Playing> {
        finishGame(data = helper.resign())
    }

    fun onReveal(): Unit = runAs<BlindModeUiState.Playing> { state ->
        if (!state.isRevealAvailable) return

        viewModelScope.launch {
            _uiState.updateAs<BlindModeUiState.Playing> { it.copy(isRevealing = true, isRevealAvailable = it.isTrainingMode) }
            delay(REVEAL_DURATION_MS)
            _uiState.updateAs<BlindModeUiState.Playing> { it.copy(isRevealing = false) }
        }
    }

    fun onPlayAgain() = runAs<BlindModeUiState.GameOver> { state ->
        viewModelScope.launch {
            orchestrator.stop()
            _uiState.update { BlindModeUiState.Setup(isTrainingMode = state.isTrainingMode) }
        }
    }

    fun onBackPressed(): Boolean {
        if (_uiState.value !is BlindModeUiState.Playing) return false
        _uiState.updateAs<BlindModeUiState.Playing> { it.copy(isAbandonDialogShown = true) }
        return true
    }

    fun onAbandonConfirmed() = runAs<BlindModeUiState.Playing> {
        finishGame(data = helper.resign())
    }

    fun onAbandonDismissed() = _uiState.updateAs<BlindModeUiState.Playing> { it.copy(isAbandonDialogShown = false) }

    private fun requestEngineMove() {
        _uiState.updateAs<BlindModeUiState.Playing> { it.copy(isThinking = true) }
        viewModelScope.launch {
            val engineMove = orchestrator.requestEngineMove()
            val data = helper.playMove(engineMove)
            if (data.isOver) {
                finishGame(data = data)
            } else {
                _uiState.updateAs<BlindModeUiState.Playing> { it.copy(data = data, isThinking = false, moveHistory = helper.algebraicHistory()) }
            }
        }
    }

    private fun finishGame(data: PlayableData) {
        viewModelScope.launch {
            onComplete(
                BlindModeGameResult(
                    isPlayerWin = data.outcome == PlayableData.Outcome.Won,
                    movesPlayed = helper.completedMoves(),
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
                moveHistory = helper.algebraicHistory(),
            )
        }
    }

    private inline fun <reified T> runAs(block: (T) -> Unit) {
        val state = _uiState.value
        if (state !is T) return
        block(state)
    }

    private inline fun playInteractive(crossinline block: (BlindModeUiState.Playing) -> Unit): Unit = runAs<BlindModeUiState.Playing> {
        if (it.isThinking || it.isRevealing) return
        block(it)
    }

    private inline fun <reified T> MutableStateFlow<BlindModeUiState>.updateAs(crossinline function: (T) -> BlindModeUiState) =
        update { if (it is T) function(it) else it }

    companion object {
        internal const val REVEAL_DURATION_MS = 3000L
    }
}
