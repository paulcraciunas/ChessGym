package com.paulcraciunas.game.logic.impl

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.GameInteractor
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import kotlin.collections.set

class RealGameInteractor : GameInteractor {
    private val _captured = mutableMapOf<Side, MutableList<Piece>>().apply {
        this[Side.WHITE] = mutableListOf()
        this[Side.BLACK] = mutableListOf()
    }
    private var _game: Game? = null
    private var _player: Side? = null
    private val game: Game
        get() = _game!!
    override var captured = _captured
    override val rating: Int?
        get() = game.rating
    override val player: Side
        get() = _player!!
    override val lastPly: Ply?
        get() = game.info.lastPly

    override fun load(game: Game, player: Side) {
        _game = game
        _player = player

        game.start()
        updateCaptured()
    }

    override fun canPlay(from: Locus, to: Locus): Boolean = game.ply(from, to) != null
    override fun moves(from: Locus): List<Locus> = game.plies(from).map { it.to }
    override fun play(from: Locus, to: Locus) {
        assert(canPlay(from, to))
        game.play(from, to)
        updateCaptured()
    }

    override fun canPromote(from: Locus, to: Locus): Boolean = game.ply(from, to)?.isPromotion() ?: false
    override fun promote(from: Locus, to: Locus, result: Piece) {
        assert(canPromote(from, to))

        game.ply(from, to)!!.promote(result)
        play(from, to)
    }

    override fun resign() {
        game.resign()
    }

    override fun isOver(): Boolean = game.state is Game.GameState.Finished

    private fun updateCaptured() {
        _captured[Side.WHITE]!!.clear()
        _captured[Side.BLACK]!!.clear()
        for (side in Side.entries) {
            for (piece in Piece.entries) {
                val missing = piece.startingCount() - game.board.pieces(side, piece).size
                (0 until missing).forEach { _ ->
                    _captured[side.other()]!!.add(piece)
                }
            }
        }
    }
}
