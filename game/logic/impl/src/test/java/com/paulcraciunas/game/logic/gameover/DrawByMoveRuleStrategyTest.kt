package com.paulcraciunas.game.logic.gameover

import com.paulcraciunas.game.logic.api.Game
import com.paulcraciunas.game.logic.api.Result
import com.paulcraciunas.game.logic.impl.gameover.DrawByMoveRuleStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DrawByMoveRuleStrategyTest {
    private val helper = GameTestHelper()

    private val underTest = DrawByMoveRuleStrategy()

    @Test
    fun `GIVEN plie clock at exactly 100 moves WHEN strategy is applied THEN game state is draw by move rule`() {
        // Given
        val game = helper.createGameWithKingsOnly()
        game.start()

        // Set the 50-move rule condition (100 plies)
        game.info.plieClock = 100

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByMoveRule, (result as Game.GameState.Finished).result)
    }

    @Test
    fun `GIVEN plie clock at 99 moves WHEN strategy is applied THEN game state unchanged`() {
        // Given
        val game = helper.createGameWithKingsOnly()
        game.start()

        game.info.plieClock = 99
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN plie clock at 0 moves WHEN strategy is applied THEN game state unchanged`() {
        // Given
        val game = helper.createGameWithKingsOnly()
        game.start()

        game.info.plieClock = 0
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN plie clock at 50 moves WHEN strategy is applied THEN game state unchanged`() {
        // Given
        val game = helper.createGameWithKingsOnly()
        game.start()

        game.info.plieClock = 50
        val originalState = game.state

        // When
        val result = underTest(game)

        // Then
        assertEquals(originalState, result)
    }

    @Test
    fun `GIVEN plie clock greater than 100 moves WHEN strategy is applied THEN game state is draw by move rule`() {
        // Given
        val game = helper.createGameWithKingsOnly()
        game.start()

        game.info.plieClock = 150

        // When
        val result = underTest(game)

        // Then
        assertTrue(result is Game.GameState.Finished)
        assertEquals(Result.DrawByMoveRule, (result as Game.GameState.Finished).result)
    }
}
