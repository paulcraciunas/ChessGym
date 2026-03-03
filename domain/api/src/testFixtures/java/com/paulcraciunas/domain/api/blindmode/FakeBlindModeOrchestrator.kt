package com.paulcraciunas.domain.api.blindmode

import com.paulcraciunas.game.logic.api.Ply
import com.paulcraciunas.game.logic.api.Side
import com.paulcraciunas.game.logic.api.board.IBoard
import com.paulcraciunas.game.logic.api.board.Locus
import java.util.LinkedList

// TODO Paul: can we implement a proper Fake here, with predefined moves for black (e.g the Spanish game)
class FakeBlindModeOrchestrator : BlindModeOrchestrator {
    var startedWithElo: Int? = null
        private set
    var wasResigned: Boolean = false
        private set
    var wasReset: Boolean = false
        private set
    var playerSide: Side = Side.WHITE

    private var currentFen: String = DEFAULT_FEN

    private val selectionResults: LinkedList<SelectionResult> = LinkedList()
    private val playResults: LinkedList<PlayResult> = LinkedList()
    private val engineResults: LinkedList<EnginePlayResult> = LinkedList()
    private val moveHistoryList: MutableList<Ply> = mutableListOf()
    private val pliesFromResults: LinkedList<List<Locus>> = LinkedList()

    fun enqueueSelectionResult(vararg results: SelectionResult) {
        selectionResults.addAll(results)
    }

    fun enqueuePlayResult(vararg results: PlayResult) {
        playResults.addAll(results)
    }

    fun enqueueEngineResult(vararg results: EnginePlayResult) {
        engineResults.addAll(results)
    }

    fun enqueuePliesFrom(vararg results: List<Locus>) {
        pliesFromResults.addAll(results)
    }

    fun addMoveToHistory(ply: Ply) {
        moveHistoryList.add(ply)
    }

    fun setCurrentFen(fen: String) {
        currentFen = fen
    }

    override suspend fun startGame(elo: Int, side: Side) {
        startedWithElo = elo
        this.playerSide = side
    }

    override fun selectSquare(locus: Locus): SelectionResult =
        selectionResults.poll() ?: SelectionResult.NoPiece

    override fun playMove(from: Locus, to: Locus): PlayResult =
        playResults.poll() ?: PlayResult.Invalid

    override suspend fun requestEngineMove(): EnginePlayResult =
        engineResults.poll() ?: throw IllegalStateException("No engine results enqueued")

    override fun resign() {
        wasResigned = true
    }

    override fun board(): IBoard {
        TODO("Not yet implemented")
    }

    override fun currentFen(): String = currentFen

    override fun moveHistory(): List<Ply> = moveHistoryList.toList()

    override fun pliesFrom(locus: Locus): List<Locus> =
        pliesFromResults.poll() ?: emptyList()

    override fun playerSide(): Side = Side.WHITE

    override suspend fun reset() {
        wasReset = true
    }

    companion object {
        const val DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}
