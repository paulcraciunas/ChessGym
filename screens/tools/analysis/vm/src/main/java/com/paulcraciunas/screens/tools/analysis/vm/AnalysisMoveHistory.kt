package com.paulcraciunas.screens.tools.analysis.vm

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.serializer.api.Serializer

/**
 * Manages move history and navigation for the analysis board.
 * Stores recorded moves and reconstructs game state on demand.
 */
internal class AnalysisMoveHistory(
    private val fenSerializer: Serializer,
) {
    private val records: MutableList<HistoryItem> = mutableListOf()
    private var initialFen: String = ""
    private var initialPlayer: Side = Side.WHITE

    var currentIndex: Int = 0
        private set

    fun initialize(fen: String, player: Side) {
        reset()
        initialFen = fen
        initialPlayer = player
    }

    fun size(): Int = records.size
    fun isAtEnd(): Boolean = currentIndex == size()
    fun isAtStart(): Boolean = currentIndex == 0

    fun recordMove(game: Game, promotionPiece: Piece? = null) {
        recordMove(
            from = game.history.last().from,
            to = game.history.last().to,
            promotionPiece = promotionPiece,
            resultFen = fenSerializer.of(game),
            sideToMove = game.info.turn
        )
    }

    fun recordMove(from: Locus, to: Locus, promotionPiece: Piece?, resultFen: String, sideToMove: Side) {
        records.add(
            HistoryItem(
                from = from,
                to = to,
                promotion = promotionPiece,
                resultFen = resultFen,
                sideToMove = sideToMove
            )
        )
        currentIndex = size()
    }

    fun jumpToStart() {
        currentIndex = 0
    }

    fun previousMove() {
        if (currentIndex > 0) currentIndex--
    }

    fun nextMove() {
        if (currentIndex < size()) currentIndex++
    }

    fun jumpToEnd() {
        currentIndex = size()
    }

    /**
     * Truncates forward history at the current position.
     */
    fun truncate() {
        val recordsToKeep = records.take(currentIndex)
        records.clear()
        records.addAll(recordsToKeep)
    }

    fun currentFen(): String =
        if (currentIndex == 0) initialFen
        else records[currentIndex - 1].resultFen

    fun currentSide(): Side =
        if (currentIndex == 0) initialPlayer
        else records[currentIndex - 1].sideToMove

    private fun reset() {
        records.clear()
        currentIndex = 0
        initialFen = ""
    }

    private data class HistoryItem(
        val from: Locus,
        val to: Locus,
        val promotion: Piece?,
        val resultFen: String,
        val sideToMove: Side,
    )
}
