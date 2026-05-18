package com.paulcraciunas.screens.common.model

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.GameInteractor
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

class GameViewModelHelper(
    private val gameInteractor: GameInteractor,
) {
    private val boardViewBuilder = BoardViewDataBuilder()

    val rating: Int?
        get() = gameInteractor.rating

    val player: Side
        get() = gameInteractor.player

    fun load(game: Game, player: Side): GameData {
        gameInteractor.load(game, player)
        boardViewBuilder.load(game)
        refreshBoardWithAnimation()

        return buildPuzzleData()
    }

    fun buildPuzzleData(): GameData = GameData(
        player = player,
        boardData = boardViewBuilder.build(),
        captured = gameInteractor.captured,
    )

    fun handleSquareClick(selection: Locus): OnSquareClick {
        var promotionAt: Locus? = null
        var movePlayed = false
        var moveFrom: Locus? = null
        if (boardViewBuilder.selected != null) {
            val current = boardViewBuilder.selected!!
            when {
                current == selection || !gameInteractor.canPlay(current, selection) -> boardViewBuilder.clearSelection()
                gameInteractor.canPromote(current, selection) -> promotionAt = selection
                else -> {
                    moveFrom = current
                    gameInteractor.play(current, selection)
                    refreshBoardWithAnimation()
                    movePlayed = true
                }
            }
        } else {
            val moves = gameInteractor.moves(selection)
            if (moves.isNotEmpty()) {
                boardViewBuilder.withSelection(selection, moves)
            }
        }
        return OnSquareClick(
            data = buildPuzzleData(),
            promotion = promotionAt?.let { Promotion(showChooser = true, at = it) },
            isOver = gameInteractor.isOver(),
            movePlayed = movePlayed,
            moveFrom = moveFrom,
        )
    }

    fun promote(to: Piece, at: Locus): OnSquareClick {
        gameInteractor.promote(boardViewBuilder.selected!!, at, to)
        refreshBoardWithAnimation()

        return OnSquareClick(
            data = buildPuzzleData(),
            promotion = null,
            isOver = gameInteractor.isOver(),
        )
    }

    fun playMove(from: Locus, to: Locus, promotion: Piece? = null): GameData {
        if (promotion != null) {
            gameInteractor.promote(from, to, promotion)
        } else {
            gameInteractor.play(from, to)
        }
        refreshBoardWithAnimation()
        return buildPuzzleData()
    }

    fun resign() {
        gameInteractor.resign()
    }

    private fun refreshBoardWithAnimation() {
        boardViewBuilder.refresh()
        gameInteractor.lastPly?.let { lastPly ->
            boardViewBuilder.withAnimatingPiece(lastPly.from, lastPly.to)
        }
    }

    data class OnSquareClick(
        val data: GameData,
        val promotion: Promotion?,
        val isOver: Boolean,
        val movePlayed: Boolean = false,
        val moveFrom: Locus? = null,
    )

    data class Promotion(
        val showChooser: Boolean,
        val at: Locus,
    )
}

data class GameData(
    val player: Side,
    val boardData: BoardViewData,
    val captured: Map<Side, List<Piece>>,
)
