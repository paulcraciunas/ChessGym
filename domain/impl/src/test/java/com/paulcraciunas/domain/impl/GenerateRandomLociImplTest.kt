package com.paulcraciunas.domain.impl

import com.paulcraciunas.domain.api.RandomFactory
import com.paulcraciunas.game.logic.api.board.loc
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GenerateRandomLociImplTest {
    private val fakeRandomFactory = FakeRandomFactory()
    private val underTest = GenerateRandomLociImpl(fakeRandomFactory)

    @Test
    fun `GIVEN random returns 0,0 WHEN invoke THEN returns a1`() {
        // Given
        fakeRandomFactory.setNextValues(0, 0)

        // When
        val result = underTest()

        // Then
        assertEquals("a1".loc(), result)
    }

    @Test
    fun `GIVEN random returns 7,7 WHEN invoke THEN returns h8`() {
        // Given
        fakeRandomFactory.setNextValues(7, 7)

        // When
        val result = underTest()

        // Then
        assertEquals("h8".loc(), result)
    }

    @Test
    fun `GIVEN random returns 4,4 WHEN invoke THEN returns e5`() {
        // Given
        fakeRandomFactory.setNextValues(4, 4)

        // When
        val result = underTest()

        // Then
        assertEquals("e5".loc(), result)
    }

    @Test
    fun `GIVEN random returns different values WHEN invoke THEN returns correct locus`() {
        // Given
        fakeRandomFactory.setNextValues(2, 5)

        // When
        val result = underTest()

        // Then
        assertEquals("c6".loc(), result)
    }

    @Test
    fun `GIVEN multiple calls WHEN invoke multiple times THEN generates different loci`() {
        // Given
        fakeRandomFactory.setNextValues(0, 0, 3, 4, 7, 7)

        // When
        val first = underTest()
        val second = underTest()
        val third = underTest()

        // Then
        assertEquals("a1".loc(), first)
        assertEquals("d5".loc(), second)
        assertEquals("h8".loc(), third)
    }

    @Test
    fun `GIVEN randomFactory WHEN invoke THEN calls nextInt with correct range`() {
        // Given
        fakeRandomFactory.setNextValues(0, 0)

        // When
        underTest()

        // Then
        assertEquals(listOf(Pair(0, 8), Pair(0, 8)), fakeRandomFactory.callHistory)
    }
}

private class FakeRandomFactory : RandomFactory {
    private val values = mutableListOf<Int>()
    private var currentIndex = 0
    val callHistory = mutableListOf<Pair<Int, Int>>()

    fun setNextValues(vararg nextValues: Int) {
        values.clear()
        values.addAll(nextValues.toList())
        currentIndex = 0
        callHistory.clear()
    }

    override fun nextInt(from: Int, to: Int): Int {
        callHistory.add(Pair(from, to))
        return if (currentIndex < values.size) {
            values[currentIndex++]
        } else {
            0
        }
    }
}
