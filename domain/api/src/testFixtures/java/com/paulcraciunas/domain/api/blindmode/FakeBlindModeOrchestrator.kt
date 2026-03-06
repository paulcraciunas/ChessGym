package com.paulcraciunas.domain.api.blindmode

import com.paulcraciunas.game.engine.api.EngineMove
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.game.logic.impl.RealGameFactory
import java.util.LinkedList

/**
 * A proper fake for [BlindModeOrchestrator] backed by a real [Game].
 * Plays actual chess moves so that board state, move history, and legal moves
 * are all consistent without needing stubs or enqueued results.
 *
 * Engine moves are provided via [enqueueEngineMove] and played as real game plies.
 */
class FakeBlindModeOrchestrator : BlindModeOrchestrator {
    private val gameFactory = RealGameFactory()
    private lateinit var game: Game
    private var playerSide: Side = Side.WHITE

    private val engineMoves: LinkedList<EngineMove> = LinkedList()

    var startedWithElo: Int? = null
        private set
    var wasResigned: Boolean = false
        private set
    var wasReset: Boolean = false
        private set
    var isInitialized: Boolean = false
        private set

    fun enqueueEngineMove(vararg moves: EngineMove) {
        engineMoves.addAll(moves)
    }

    override suspend fun initialize() {
        isInitialized = true
    }

    override suspend fun startGame(elo: Int, side: Side) {
        game = gameFactory.builder().withDefaultBoard().buildGame()
        game.start()
        playerSide = side
        startedWithElo = elo
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

        if (ply.isPromotion()) return PlayResult.PromotionRequired

        game.play(ply)
        return ply.toPlayResult()
    }

    override fun playMove(from: Locus, to: Locus, promotion: Piece): PlayResult {
        val validPlies = game.plies(from)
        val ply = validPlies.find { it.to == to } ?: return PlayResult.Invalid

        ply.promote(promotion)
        game.play(ply)
        return ply.toPlayResult()
    }

    private fun Ply.toPlayResult(): PlayResult =
        if (game.state is Game.GameState.Finished) {
            PlayResult.GameOver(
                ply = this,
                result = (game.state as Game.GameState.Finished).result,
            )
        } else {
            PlayResult.Success(ply = this)
        }

    override suspend fun requestEngineMove(): EnginePlayResult {
        val engineMove = engineMoves.poll()
            ?: throw IllegalStateException("FakeBlindModeOrchestrator: no engine moves enqueued")

        val ply = game.plies(engineMove.from).first { it.to == engineMove.to }
        engineMove.promotion?.let { ply.promote(it) }
        game.play(ply)

        return if (game.state is Game.GameState.Finished) {
            EnginePlayResult.GameOver(
                ply = ply,
                result = (game.state as Game.GameState.Finished).result,
            )
        } else {
            EnginePlayResult.Success(ply = ply)
        }
    }

    override fun resign() {
        wasResigned = true
        game.resign()
    }

    override fun board(): IBoard = game.board
    override fun currentFen(): String = "fake-fen"
    override fun moveHistory(): List<Ply> = game.history
    override fun pliesFrom(locus: Locus): List<Locus> = game.plies(locus).map { it.to }
    override fun playerSide(): Side = playerSide

    override suspend fun reset() {
        wasReset = true
    }
}
