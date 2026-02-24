package com.paulcraciunas.screens.about.vm

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AboutViewModelTest {

    private lateinit var underTest: AboutViewModel

    @BeforeEach
    fun setUp() {
        underTest = AboutViewModel()
    }

    @Nested
    inner class Initialization {
        @Test
        fun `GIVEN default state WHEN initialized THEN libraries list is not empty`() {
            val state = underTest.uiState.value

            assertTrue(state.libraries.isNotEmpty())
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN each library has a name`() {
            val state = underTest.uiState.value

            state.libraries.forEach { library ->
                assertTrue(library.name.isNotBlank())
            }
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN each library has a url`() {
            val state = underTest.uiState.value

            state.libraries.forEach { library ->
                assertTrue(library.url.isNotBlank())
            }
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN each library has a license`() {
            val state = underTest.uiState.value

            state.libraries.forEach { library ->
                assertTrue(library.license.isNotBlank())
            }
        }

        @Test
        fun `GIVEN default state WHEN initialized THEN no duplicate libraries exist`() {
            val state = underTest.uiState.value

            val uniqueNames = state.libraries.map { it.name }.toSet()
            assertTrue(uniqueNames.size == state.libraries.size)
        }
    }

    @Nested
    inner class Interactions {
        @Test
        fun `GIVEN initialized WHEN donate clicked THEN state remains unchanged`() {
            val stateBefore = underTest.uiState.value

            underTest.onDonateClicked()

            val stateAfter = underTest.uiState.value
            assertTrue(stateBefore == stateAfter)
        }

        @Test
        fun `GIVEN initialized WHEN rate app clicked THEN state remains unchanged`() {
            val stateBefore = underTest.uiState.value

            underTest.onRateAppClicked()

            val stateAfter = underTest.uiState.value
            assertTrue(stateBefore == stateAfter)
        }
    }
}
