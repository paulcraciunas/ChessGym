package com.paulcraciunas.screens.tools.importgame.vm

import com.paulcraciunas.domain.api.analysis.GameAnalysisProgress
import com.paulcraciunas.domain.api.analysis.MoveAnalysis
import com.paulcraciunas.domain.api.analysis.MoveClassification
import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.state.MetaData
import com.paulcraciunas.screens.data.BoardState
import com.paulcraciunas.screens.data.BoardViewData
import com.paulcraciunas.screens.data.CapturedPieces

internal class ImportGameUiStateAdapter {
    fun adapt(game: Game): ImportGameUiState.Loading = ImportGameUiState.Loading(
        boardState = boardState(game),
        playerInfo = game.metadata.toUi(),
        orientation = Side.WHITE,
    )

    fun adapt(currentState: ImportGameUiState, progress: GameAnalysisProgress): ImportGameUiState = when (progress) {
        is GameAnalysisProgress.Analyzing -> progress(currentState, progress)
        is GameAnalysisProgress.Completed -> onAnalysisCompleted(currentState)
    }

    fun update(state: ImportGameUiState.Complete, game: Game): ImportGameUiState.Complete = state.copy(
        boardState = boardState(game),
        currentMoveIndex = game.currentMoveIndex,
        canNavigateBack = game.canUndo(),
        canNavigateForward = game.canReplay(),
        blunderOverlay = blunderOverlay(state, game.currentMoveIndex),
    )

    fun boardState(game: Game): BoardState = BoardState(
        player = game.info.turn,
        rating = null,
        id = null,
        boardData = BoardViewData.from(game.board),
        promotion = null,
        movePlayed = false,
        captured = CapturedPieces("", ""),
        outcome = null,
    )

    private fun progress(currentState: ImportGameUiState, progress: GameAnalysisProgress.Analyzing): ImportGameUiState =
        if (currentState !is ImportGameUiState.Loading) currentState
        else currentState.copy(
            analysedMoves = currentState.analysedMoves + progress.latestMoveAnalysis.toUi(),
            progressPercent = (progress.currentMove * 100) / progress.totalMoves,
        )

    private fun onAnalysisCompleted(currentState: ImportGameUiState): ImportGameUiState =
        if (currentState !is ImportGameUiState.Loading) currentState
        else ImportGameUiState.Complete(
            boardState = currentState.boardState,
            orientation = currentState.orientation,
            playerInfo = currentState.playerInfo,
            analysedMoves = currentState.analysedMoves,
            currentMoveIndex = currentState.analysedMoves.size,
            canNavigateBack = true,
            canNavigateForward = false,
        )

    private fun blunderOverlay(state: ImportGameUiState.Complete, at: Int): ImportGameUiState.BlunderOverlay? {
        val move = state.analysedMoves.getOrNull(at - 1) ?: return null // -1 because idx 0 == starting board
        return if (move.classification.isBad()) {
            move.bestMove?.let { ImportGameUiState.BlunderOverlay(from = it.from, to = it.to) }
        } else null
    }
}

private fun MetaData.toUi(): ImportGameUiState.PlayerInfo {
    val white = data(MetaData.Header.White)
    val black = data(MetaData.Header.Black)
    return ImportGameUiState.PlayerInfo(whiteName = white, blackName = black)
}

private fun MoveAnalysis.toUi() = ImportGameUiState.AnalysedMove(
    algebraic = moveAlgebraic ?: "?",
    classification = classification,
    normalised = evaluation.toFraction(),
    bestMove = bestMove?.toUi(),
)

private fun EngineMove.toUi() = ImportGameUiState.BestMove(from = from, to = to, promotion = promotion)

private fun MoveClassification.isBad() = this == MoveClassification.Mistake || this == MoveClassification.Blunder