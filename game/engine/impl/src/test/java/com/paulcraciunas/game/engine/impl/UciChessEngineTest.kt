package com.paulcraciunas.game.engine.impl

import com.paulcraciunas.game.engine.impl.uci.UciCommand
import com.paulcraciunas.game.engine.impl.uci.UciResponse
import com.paulcraciunas.game.logic.api.board.File
import com.paulcraciunas.game.logic.api.board.Locus
import com.paulcraciunas.game.logic.api.board.Rank
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UciChessEngineTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeFacade = FakeUciFacade()

    private val underTest = UciChessEngine(
        dispatcher = testDispatcher,
        uci = fakeFacade,
    )

    @Test
    fun `GIVEN engine WHEN initialize THEN starts engine and sends init and ready commands`() = runTest(testDispatcher) {
        underTest.initialize()

        assertTrue(fakeFacade.isStarted)
        assertEquals(
            listOf(UciCommand.Init::class, UciCommand.IsReady::class),
            fakeFacade.executedCommands.map { it::class },
        )
    }

    @Test
    fun `GIVEN initialized engine WHEN startNewGame THEN sends newgame limitelo setelo and ready`() = runTest(testDispatcher) {
        underTest.startNewGame(elo = 1400)

        val commandTypes = fakeFacade.executedCommands.map { it::class }
        assertEquals(UciCommand.NewGame::class, commandTypes[0])
        assertEquals(UciCommand.LimitElo::class, commandTypes[1])
        assertEquals(UciCommand.SetElo::class, commandTypes[2])
        assertEquals(UciCommand.IsReady::class, commandTypes[3])

        val setElo = fakeFacade.executedCommands[2] as UciCommand.SetElo
        assertEquals(1400, setElo.elo)
    }

    @Test
    fun `GIVEN game in progress WHEN calculateBestMove THEN sends position and go commands`() = runTest(testDispatcher) {
        val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
        val expectedFrom = Locus(File.e, Rank.`7`)
        val expectedTo = Locus(File.e, Rank.`5`)
        fakeFacade.bestMoveResponse = com.paulcraciunas.game.engine.api.EngineMove(
            from = expectedFrom,
            to = expectedTo,
        )

        val result = underTest.calculateBestMove(fen)

        assertEquals(expectedFrom, result.from)
        assertEquals(expectedTo, result.to)

        val commandTypes = fakeFacade.executedCommands.map { it::class }
        assertEquals(UciCommand.SetPosition::class, commandTypes[0])
        assertEquals(UciCommand.SetMoveTime::class, commandTypes[1])
    }

    @Test
    fun `GIVEN engine WHEN stop THEN sends stop command`() = runTest(testDispatcher) {
        underTest.stop()

        assertEquals(1, fakeFacade.executedCommands.size)
        assertEquals(UciCommand.Stop::class, fakeFacade.executedCommands[0]::class)
    }

    @Test
    fun `GIVEN engine WHEN shutdown THEN sends quit command`() = runTest(testDispatcher) {
        underTest.shutdown()

        assertEquals(1, fakeFacade.executedCommands.size)
        assertEquals(UciCommand.Quit::class, fakeFacade.executedCommands[0]::class)
    }
}

private class FakeUciFacade : UciFacade {
    val executedCommands: MutableList<UciCommand> = mutableListOf()
    var isStarted: Boolean = false
        private set
    var bestMoveResponse: com.paulcraciunas.game.engine.api.EngineMove? = null

    override fun startEngine() {
        isStarted = true
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : UciResponse> execute(uciCommand: UciCommand): T {
        executedCommands.add(uciCommand)
        val response: UciResponse = when (uciCommand) {
            is UciCommand.Init -> UciResponse.Initialized
            is UciCommand.IsReady -> UciResponse.Ready
            is UciCommand.SetMoveTime -> UciResponse.BestMove(
                engineMove = bestMoveResponse
                    ?: throw IllegalStateException("No bestMoveResponse configured"),
            )
            else -> UciResponse.Done
        }
        return response as T
    }
}
