package com.paulcraciunas.screens.tools.importgame.vm

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import com.paulcraciunas.screens.common.model.BoardViewData
import com.paulcraciunas.screens.common.model.BoardViewDataBuilder
import com.paulcraciunas.serializer.api.Serializer

/**
 * Manages move history, board snapshots, and navigation state for imported games.
 *
 * Separates the concern of tracking board positions and recorded moves from the ViewModel,
 * which focuses on UI state orchestration.
 */
internal class MoveHistory(
    private val fenSerializer: Serializer,
) {
    private val snapshots: MutableList<BoardViewData> = mutableListOf()
    private val moves: MutableList<RecordedMove> = mutableListOf()

    private var originalImportText: String = ""

    var currentIndex: Int = 0
        private set

    val totalMoves: Int get() = snapshots.size - 1

    val isAtLatestPosition: Boolean get() = currentIndex >= totalMoves

    val currentSnapshot: BoardViewData
        get() = snapshots.getOrElse(currentIndex) { snapshots.last() }

    /**
     * Initializes history for a FEN import. Creates a single snapshot for the initial position.
     */
    fun initForFen(game: Game, importText: String) {
        reset()
        originalImportText = importText
        snapshots.add(BoardViewDataBuilder.fromBoard(game.board))
    }

    /**
     * Initializes history for a PGN import. Replays all moves on a fresh game to build
     * a complete set of board snapshots for navigation.
     */
    fun initForPgn(finishedGame: Game, importText: String) {
        reset()
        originalImportText = importText

        val recordedMoves = finishedGame.history.map { ply ->
            RecordedMove(ply.from, ply.to, ply.extractPromotionPiece())
        }

        val freshGame = fenSerializer.from(STARTING_FEN)
        freshGame.start()
        snapshots.add(BoardViewDataBuilder.fromBoard(freshGame.board))

        for (move in recordedMoves) {
            replayMove(freshGame, move)
            snapshots.add(BoardViewDataBuilder.fromBoard(freshGame.board))
            moves.add(move)
        }

        currentIndex = totalMoves
    }

    /**
     * Records a move played by the user and adds a new board snapshot.
     */
    fun recordMove(game: Game, from: Locus, to: Locus, promotionPiece: Piece?) {
        moves.add(RecordedMove(from, to, promotionPiece))
        snapshots.add(BoardViewDataBuilder.fromBoard(game.board))
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
     * Truncates forward history at the current position and reconstructs the game
     * so the user can play new moves from this point.
     *
     * @return the reconstructed [Game] at the current position, or null on failure
     */
    fun truncateAndReconstruct(): Game? {
        val movesToKeep = moves.take(currentIndex)
        moves.clear()
        moves.addAll(movesToKeep)

        while (snapshots.size > currentIndex + 1) {
            snapshots.removeAt(snapshots.size - 1)
        }

        return reconstructGame()
    }

    private fun reconstructGame(): Game? {
        return try {
            val game = fenSerializer.from(originalImportText)
            if (game.state == Game.GameState.Ready) {
                game.start()
            }
            for (move in moves) {
                replayMove(game, move)
            }
            game
        } catch (_: Exception) {
            null
        }
    }

    private fun replayMove(game: Game, move: RecordedMove) {
        val ply = game.plies(move.from).first { it.to == move.to }
        move.promotionPiece?.let { ply.promote(it) }
        game.play(ply)
    }

    private fun reset() {
        snapshots.clear()
        moves.clear()
        currentIndex = 0
        originalImportText = ""
    }

    companion object {
        internal const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}

private fun Ply.extractPromotionPiece(): Piece? {
    if (!isPromotion()) return null
    val alg = algebraic()
    val idx = alg.indexOf('=')
    if (idx < 0 || idx + 1 >= alg.length) return null
    return when (alg[idx + 1]) {
        'Q' -> Piece.Queen
        'R' -> Piece.Rook
        'B' -> Piece.Bishop
        'N' -> Piece.Knight
        else -> Piece.Queen
    }
}
