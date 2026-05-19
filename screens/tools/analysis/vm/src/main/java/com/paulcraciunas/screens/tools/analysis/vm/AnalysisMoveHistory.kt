package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.serializer.api.Serializer
import timber.log.Timber

internal data class RecordedMove(
    val from: Locus,
    val to: Locus,
    val promotionPiece: Piece?,
)

/**
 * Manages move history and navigation for the analysis board.
 * Stores recorded moves and reconstructs game state on demand.
 */
internal class AnalysisMoveHistory(
    private val fenSerializer: Serializer,
) {
    private val moves: MutableList<RecordedMove> = mutableListOf()
    private var initialFen: String = ""

    var currentIndex: Int = 0
        private set
    val totalMoves: Int get() = moves.size
    val isAtLatestPosition: Boolean get() = currentIndex >= totalMoves

    fun initialize(fen: String) {
        reset()
        initialFen = fen
    }

    fun recordMove(from: Locus, to: Locus, promotionPiece: Piece?) {
        moves.add(RecordedMove(from, to, promotionPiece))
        currentIndex = totalMoves
    }

    fun jumpToStart() {
        currentIndex = 0
    }

    fun previousMove() {
        if (currentIndex > 0) currentIndex--
    }

    fun nextMove() {
        if (currentIndex < totalMoves) currentIndex++
    }

    fun jumpToEnd() {
        currentIndex = totalMoves
    }

    /**
     * Truncates forward history at the current position.
     */
    fun truncate() {
        val movesToKeep = moves.take(currentIndex)
        moves.clear()
        moves.addAll(movesToKeep)
    }

    fun reconstructAtCurrentIndex(): Game? = reconstructAtIndex(currentIndex)
    fun reconstructAtIndex(index: Int): Game? {
        return try {
            val game = fenSerializer.from(initialFen)
            if (game.state == Game.GameState.Ready) {
                game.start()
            }
            val movesToReplay = moves.take(index)
            for (move in movesToReplay) {
                val ply = game.plies(move.from).first { it.to == move.to }
                move.promotionPiece?.let { ply.promote(it) }
                game.play(ply)
            }
            game
        } catch (e: Exception) {
            Timber.w(e, "Failed to reconstruct game for analysis at index $index")
            null
        }
    }

    fun sideToMoveAt(index: Int): Side {
        val game = reconstructAtIndex(index) ?: return Side.WHITE
        return game.info.turn
    }

    fun fenAtIndex(index: Int): String {
        if (index == 0) return initialFen
        val game = reconstructAtIndex(index) ?: return initialFen
        return fenSerializer.of(game)
    }

    private fun reset() {
        moves.clear()
        currentIndex = 0
        initialFen = ""
    }
}
