package com.paulcraciunas.screens.data

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Piece
import kotlinx.coroutines.delay

class BoardInteractionHelper(
    private val navigation: NavigationStrategy = NoOpNavigation,
    private val solution: SolutionStrategy = NoOpSolution,
) {
    private lateinit var playable: PlayableBoard
    private lateinit var data: PlayableData
    var autoPromote: Boolean = false

    fun load(playable: PlayableBoard): PlayableData {
        this.playable = playable
        playable.initialize()
        navigation.load(playable)
        solution.load(playable)
        data = PlayableData(
            rating = playable.rating,
            player = playable.player,
            id = playable.id,
            boardData = BoardViewData.from(
                board = playable.board,
                lastMove = playable.info.lastPly.asPair()
            ),
            captured = updateCaptured(),
            isOver = false,
            outcome = null,
        )
        return data
    }

    fun isLoaded(): Boolean = ::data.isInitialized
    fun current(): PlayableData = data
    fun toMove(): Side = playable.info.turn

    fun handleSquareClick(selection: Locus): ClickResult {
        if (playable.isOver()) return ClickResult(data = data, promotion = null, movePlayed = false)
        var promotion: Promotion? = null
        var movePlayed = false
        data = if (data.boardData.selection != null) {
            val current = data.boardData.selection!!
            when {
                current == selection || !canPlay(current, selection) -> {
                    data.copy(boardData = data.boardData.clearSelection())
                }
                canPromote(current, selection) -> {
                    if (autoPromote) {
                        movePlayed = true
                        data.copy(
                            boardData = promote(from = current, to = selection, result = Piece.Queen),
                            captured = updateCaptured(),
                            isOver = playable.isOver(),
                            outcome = playable.outcome(),
                        )
                    } else {
                        promotion = Promotion(showChooser = true, at = selection)
                        data
                    }
                }
                else -> {
                    movePlayed = true
                    data.copy(
                        boardData = play(current, selection),
                        captured = updateCaptured(),
                        isOver = playable.isOver(),
                        outcome = playable.outcome(),
                    )
                }
            }
        } else if (playable.board.has(playable.activeSide, selection)) {
            data.copy(boardData = data.boardData.select(at = selection, moves = moves(from = selection)))
        } else {
            data
        }
        return ClickResult(
            data = data,
            promotion = promotion,
            movePlayed = movePlayed,
        )
    }

    fun promote(to: Piece, at: Locus): ClickResult = ClickResult(
        data = data.copy(
            boardData = promote(from = data.boardData.selection!!, to = at, result = to),
            captured = updateCaptured(),
            isOver = playable.isOver(),
            outcome = playable.outcome(),
        ),
        promotion = null,
        movePlayed = true,
    )

    fun playMove(from: Locus, to: Locus, promotion: Piece? = null): PlayableData {
        val boardData = if (promotion != null) {
            promote(from, to, promotion)
        } else {
            play(from, to)
        }
        data = data.copy(boardData = boardData, captured = updateCaptured(), isOver = playable.isOver(), outcome = playable.outcome())
        return data
    }

    fun playMove(ply: Ply): PlayableData {
        assert(playable.ply(ply.from, ply.to) != null)
        playable.play(ply)
        data = data.copy(boardData = reloadBoard(), captured = updateCaptured(), isOver = playable.isOver(), outcome = playable.outcome())
        return data
    }

    fun selectSquare(at: Locus): PlayableData {
        data = data.copy(boardData = data.boardData.clearSelection().select(at, moves(from = at)))
        return data
    }

    fun refresh(): PlayableData {
        data = data.copy(
            boardData = BoardViewData.from(
                board = playable.board,
                lastMove = playable.info.lastPly.asPair()
            ),
            captured = updateCaptured(),
            isOver = playable.isOver(),
            outcome = playable.outcome(),
        )
        return data
    }

    fun resign(): PlayableData {
        playable.resign()
        return refresh()
    }

    fun completedMoves(): Int = navigation.size()
    fun algebraicHistory(): String = navigation.algebraic()
    fun canUndo(): Boolean = navigation.canUndo()
    fun canReplay(): Boolean = navigation.canReplay()
    fun undoLast(): PlayableData = navigate(::canUndo, navigation::undoLast)
    fun undoAll(): PlayableData = navigate(::canUndo, navigation::undoAll)
    fun replayNext(): PlayableData = navigate(::canReplay, navigation::replayNext)
    fun replayAll(): PlayableData = navigate(::canReplay, navigation::replayAll)

    fun hint(): PlayableData {
        val hintSquare = solution.hintSquare() ?: return data
        return selectSquare(hintSquare)
    }

    fun playNextSolutionMove(): PlayableData? {
        if (!solution.playNextSolutionMove()) return null
        return refresh()
    }

    suspend fun playSolution(updateUiState: (PlayableData) -> Boolean) {
        while (solution.hasSolutionMoves()) {
            delay(PuzzleSolution.SOLUTION_MOVE_DELAY_MS)
            val nextData = playNextSolutionMove() ?: break
            if (!updateUiState(nextData)) break
        }
    }

    fun hasSolutionMoves(): Boolean = solution.hasSolutionMoves()

    private inline fun navigate(guard: () -> Boolean, action: () -> Unit): PlayableData {
        if (!guard()) return data
        action()
        return refresh()
    }

    private fun moves(from: Locus): List<Locus> = playable.plies(from).map { it.to }
    private fun canPlay(from: Locus, to: Locus): Boolean = playable.ply(from, to) != null
    private fun canPromote(from: Locus, to: Locus): Boolean = playable.ply(from, to)?.isPromotion() ?: false

    private fun promote(from: Locus, to: Locus, result: Piece): BoardViewData {
        assert(canPromote(from, to))
        playable.ply(from, to)!!.promote(result)
        return play(from, to)
    }

    private fun play(from: Locus, to: Locus): BoardViewData {
        assert(canPlay(from, to))
        playable.play(from, to)
        return reloadBoard()
    }

    private fun reloadBoard(): BoardViewData = BoardViewData.from(
        board = playable.board,
        lastMove = playable.info.lastPly.asPair(),
        withAnimation = true
    )

    private fun updateCaptured(): CapturedPieces {
        val playerCaptured = mutableListOf<Piece>()
        val otherCaptured = mutableListOf<Piece>()
        worthSortedPieces.forEach { piece ->
            val otherCount = (piece.defaultCount - playable.board.pieces(playable.player.other(), piece).size).coerceAtLeast(0)
            val playerCount = (piece.defaultCount - playable.board.pieces(playable.player, piece).size).coerceAtLeast(0)
            repeat(otherCount) { playerCaptured.add(piece) }
            repeat(playerCount) { otherCaptured.add(piece) }
        }
        return CapturedPieces(
            byPlayer = playerCaptured.joinToString(separator = "") { it.unicode },
            byOpponent = otherCaptured.joinToString(separator = "") { it.unicode },
        )
    }
}

private fun Ply?.asPair(): Pair<Locus, Locus>? = if (this != null) from to to else null
private val worthSortedPieces = listOf(Piece.Queen, Piece.Rook, Piece.Bishop, Piece.Knight, Piece.Pawn)
