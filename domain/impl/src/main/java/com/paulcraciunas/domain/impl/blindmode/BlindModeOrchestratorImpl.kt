package com.paulcraciunas.domain.impl.blindmode

import com.paulcraciunas.domain.api.blindmode.BlindModeOrchestrator
import com.paulcraciunas.domain.api.blindmode.EnginePlayResult
import com.paulcraciunas.domain.api.blindmode.PlayResult
import com.paulcraciunas.domain.api.blindmode.SelectionResult
import com.paulcraciunas.game.engine.api.ChessEngine
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Game.GameState
import com.paulcraciunas.game.logic.api.GameFactory
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.serializer.api.Serializer

class BlindModeOrchestratorImpl(
    private val gameFactory: GameFactory,
    private val chessEngine: ChessEngine,
    private val serializer: Serializer,
) : BlindModeOrchestrator {

    private lateinit var game: Game
    private var playerSide: Side = Side.WHITE

    override suspend fun startGame(elo: Int, side: Side) {
        game = gameFactory.builder().withDefaultBoard().buildGame()
        game.start()
        // TODO Paul: integrate setting the side in the game engine
        playerSide = side
        chessEngine.startNewGame(elo)
    }

    override fun selectSquare(locus: Locus): SelectionResult {
        game.board.at(locus) ?: return SelectionResult.NoPiece
        if (!game.board.has(playerSide, locus)) return SelectionResult.WrongSide

        val legalMoves = game.plies(locus).map { it.to }
        return SelectionResult.PieceSelected(locus = locus, legalMoves = legalMoves)
    }

    override fun playMove(from: Locus, to: Locus): PlayResult {
        val validPlies = game.plies(from)
        val ply = validPlies.find { it.to == to } ?: return PlayResult.Invalid

        game.play(ply)
        return ply.toPlayResult(with = game.state)
    }

    override suspend fun requestEngineMove(): EnginePlayResult {
        val fen = serializer.of(game)
        val engineMove = chessEngine.calculateBestMove(fen)

        val ply = game.plies(engineMove.from)
            .first { it.to == engineMove.to }

        engineMove.promotion?.let {
            ply.promote(it)
        }

        game.play(ply)
        return ply.toEngineResult(with = game.state)
    }

    override fun resign() {
        game.resign()
    }

    override fun board(): IBoard = game.board
    override fun currentFen(): String = serializer.of(game)
    override fun moveHistory(): List<Ply> = game.history
    override fun pliesFrom(locus: Locus): List<Locus> = game.plies(locus).map { it.to }
    override fun playerSide(): Side = playerSide

    override suspend fun reset() {
        chessEngine.stop()
    }
}

private fun Ply.toPlayResult(with: GameState): PlayResult {
    return if (with is GameState.Finished) {
        PlayResult.GameOver(ply = this, result = with.result)
    } else {
        PlayResult.Success(ply = this)
    }
}

private fun Ply.toEngineResult(with: GameState): EnginePlayResult {
    return if (with is GameState.Finished) {
        EnginePlayResult.GameOver(ply = this, result = with.result)
    } else {
        EnginePlayResult.Success(ply = this)
    }
}
