package com.paulcraciunas.screens.tools.dashboard.vm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ToolsDashboardViewModelTest {

    private val underTest = ToolsDashboardViewModel()

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN viewModel initialized THEN uiState is not loading`() = runTest {
        // Then
        val uiState = underTest.uiState.value
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `GIVEN Clock mode WHEN onModeSelected called THEN navigation callback invoked with Clock`() = runTest {
        // Given
        val mode = ToolsMode.Clock
        var receivedMode: ToolsMode? = null

        // When
        underTest.onModeSelected(mode) { receivedMode = it }

        // Then
        assertEquals(ToolsMode.Clock, receivedMode)
    }

    @Test
    fun `GIVEN Analysis mode WHEN onModeSelected called THEN navigation callback invoked with Analysis`() = runTest {
        // Given
        val mode = ToolsMode.Analysis
        var receivedMode: ToolsMode? = null

        // When
        underTest.onModeSelected(mode) { receivedMode = it }

        // Then
        assertEquals(ToolsMode.Analysis, receivedMode)
    }

    @Test
    fun `GIVEN ImportGame mode WHEN onModeSelected called THEN navigation callback invoked with ImportGame`() = runTest {
        // Given
        val mode = ToolsMode.ImportGame
        var receivedMode: ToolsMode? = null

        // When
        underTest.onModeSelected(mode) { receivedMode = it }

        // Then
        assertEquals(ToolsMode.ImportGame, receivedMode)
    }
}
