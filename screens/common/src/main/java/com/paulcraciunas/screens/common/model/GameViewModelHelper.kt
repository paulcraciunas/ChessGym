package com.paulcraciunas.screens.common.model

import androidx.compose.runtime.Immutable
import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece

class GameViewModelHelper {
    private lateinit var gameData: GameData2
    private lateinit var player: Side
    private lateinit var game: Game
    var autoPromote: Boolean = false

    fun load(game: Game, player: Side): GameData2 {
        this.game = game
        this.player = player
        if (game.state == Game.GameState.Ready) {
            game.start()
        }
        gameData = GameData2(
            rating = game.rating,
            player = this.player,
            boardData = BoardViewData2.from(
                board = game.board,
                lastMove = game.info.lastPly.asPair()
            ),
            captured = updateCaptured(),
        )
        return gameData
    }

    fun isLoaded(): Boolean = ::gameData.isInitialized
    fun loadedGame(): Game {
        assert(isLoaded())
        return game
    }

    fun current(): GameData2 = gameData
    fun toMove(): Side = game.info.turn

    fun handleSquareClick(selection: Locus): GameOnSquareClick {
        if (isOver()) return GameOnSquareClick(data = gameData, promotion = null, isOver = true, movePlayed = false)
        var promotion: GamePromotion? = null
        var movePlayed = false
        gameData = if (gameData.boardData.selection != null) {
            val current = gameData.boardData.selection!!
            when {
                current == selection || !canPlay(current, selection) -> {
                    gameData.copy(boardData = gameData.boardData.clearSelection())
                }
                canPromote(current, selection) -> {
                    if (autoPromote) {
                        movePlayed = true
                        gameData.copy(
                            boardData = promote(from = current, to = selection, result = Piece.Queen),
                            captured = updateCaptured()
                        )
                    } else {
                        promotion = GamePromotion(showChooser = true, at = selection)
                        gameData
                    }
                }
                else -> {
                    movePlayed = true
                    gameData.copy(boardData = play(current, selection), captured = updateCaptured())
                }
            }
        } else {
            gameData.copy(boardData = gameData.boardData.select(at = selection, moves = moves(from = selection)))
        }
        return GameOnSquareClick(
            data = gameData,
            promotion = promotion,
            isOver = isOver(),
            movePlayed = movePlayed,
        )
    }

    fun promote(to: Piece, at: Locus): GameOnSquareClick = GameOnSquareClick(
        data = gameData.copy(boardData = promote(from = gameData.boardData.selection!!, to = at, result = to)),
        promotion = null,
        isOver = isOver(),
        movePlayed = true,
    )

    fun playMove(move: String): GameData2 {
        game.play(move)
        gameData = gameData.copy(boardData = reloadBoard(), captured = updateCaptured())
        return gameData
    }

    fun playMove(from: Locus, to: Locus, promotion: Piece? = null): GameData2 {
        if (promotion != null) {
            promote(from, to, promotion)
        } else {
            play(from, to)
        }
        gameData = gameData.copy(boardData = reloadBoard(), captured = updateCaptured())
        return gameData
    }

    fun resign(): GameData2 {
        game.resign()
        gameData = gameData.copy(boardData = reloadBoard(), captured = updateCaptured())
        return gameData
    }

    fun canUndo(): Boolean = game.canUndo()
    fun canReplay(): Boolean = game.canReplay()
    fun undoLast(): GameData2 = navigate(guard = game.canUndo()) { game.undoLast() }
    fun undoAll(): GameData2 = navigate(guard = game.canUndo()) { game.undoAll() }
    fun replayNext(): GameData2 = navigate(guard = game.canReplay()) { game.replayNext() }
    fun replayAll(): GameData2 = navigate(guard = game.canReplay()) { game.replayAll() }

    private fun navigate(guard: Boolean, action: () -> Unit): GameData2 {
        if (guard) {
            action()
            gameData = gameData.copy(
                boardData = BoardViewData2.from(
                    board = game.board,
                    lastMove = game.info.lastPly.asPair()
                ),
                captured = updateCaptured()
            )
        }
        return current()
    }

    @Immutable
    data class GameOnSquareClick(
        val data: GameData2,
        val promotion: GamePromotion?,
        val isOver: Boolean,
        val movePlayed: Boolean,
    )

    @Immutable
    data class GamePromotion(
        val showChooser: Boolean,
        val at: Locus,
    )

    @Immutable
    data class GameData2(
        val rating: Int?,
        val player: Side,
        val boardData: BoardViewData2,
        val captured: GameCaptured,
    ) {
        @Immutable
        data class GameCaptured(
            val byPlayer: String,
            val byOpponent: String,
        )
    }

    private fun isOver(): Boolean = game.state is Game.GameState.Finished

    private fun moves(from: Locus): List<Locus> = game.plies(from).map { it.to }
    private fun canPlay(from: Locus, to: Locus): Boolean = game.ply(from, to) != null
    private fun canPromote(from: Locus, to: Locus): Boolean = game.ply(from, to)?.isPromotion() ?: false
    private fun promote(from: Locus, to: Locus, result: Piece): BoardViewData2 {
        assert(canPromote(from, to))

        game.ply(from, to)!!.promote(result)
        return play(from, to)
    }

    private fun play(from: Locus, to: Locus): BoardViewData2 {
        assert(canPlay(from, to))
        game.play(from, to)
        return reloadBoard()
    }

    private fun reloadBoard(): BoardViewData2 = BoardViewData2.from(
        board = game.board,
        lastMove = game.info.lastPly.asPair(),
        withAnimation = true
    )

    private fun updateCaptured(): GameData2.GameCaptured {
        val playerCaptured = mutableListOf<Piece>()
        val otherCaptured = mutableListOf<Piece>()
        worthSortedPieces.forEach { piece ->
            val otherCount = (piece.defaultCount - game.board.pieces(player.other(), piece).size).coerceAtLeast(0)
            val playerCount = (piece.defaultCount - game.board.pieces(player, piece).size).coerceAtLeast(0)
            repeat(otherCount) {
                playerCaptured.add(piece)
            }
            repeat(playerCount) {
                otherCaptured.add(piece)
            }
        }
        return GameData2.GameCaptured(
            byPlayer = playerCaptured.joinToString(separator = "") { it.unicode },
            byOpponent = otherCaptured.joinToString(separator = "") { it.unicode },
        )
    }
}

private fun Ply?.asPair(): Pair<Locus, Locus>? = if (this != null) from to to else null
private val worthSortedPieces = listOf(Piece.Queen, Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Pawn)
