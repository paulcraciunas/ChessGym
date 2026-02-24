package com.paulcraciunas.screens.settings.vm

import com.paulcraciunas.settings.application.api.AppSettings
import com.paulcraciunas.settings.application.api.FakeAppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SettingsViewModelTest {
    private val appSettingsRepository = FakeAppSettingsRepository.default()
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var underTest: SettingsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Initialization {
        @Test
        fun `GIVEN default settings WHEN initialized THEN uiState reflects defaults`() = runTest {
            createViewModel()

            val state = underTest.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isHapticFeedbackEnabled)
            assertTrue(state.isAutoPromoteEnabled)
            assertTrue(state.isShowBordersEnabled)
            assertTrue(state.isHighlightLegalMovesEnabled)
            assertTrue(state.isAnimationsEnabled)
            assertEquals(AppSettings.LightMode.System, state.lightMode)
        }

        @Test
        fun `GIVEN custom settings WHEN initialized THEN uiState reflects them`() = runTest {
            appSettingsRepository.setAppSettings(
                appSettingsRepository.getCurrentSettings().copy(
                    enableVibrations = false,
                    autoPromote = false,
                    showBorders = false,
                    highlightLegalMoves = false,
                    enableAnimations = false,
                    lightMode = AppSettings.LightMode.Dark,
                )
            )

            createViewModel()

            val state = underTest.uiState.value
            assertFalse(state.isHapticFeedbackEnabled)
            assertFalse(state.isAutoPromoteEnabled)
            assertFalse(state.isShowBordersEnabled)
            assertFalse(state.isHighlightLegalMovesEnabled)
            assertFalse(state.isAnimationsEnabled)
            assertEquals(AppSettings.LightMode.Dark, state.lightMode)
        }

        @Test
        fun `GIVEN repository WHEN initialized THEN isLoading is false`() = runTest {
            createViewModel()

            assertFalse(underTest.uiState.value.isLoading)
        }
    }

    @Nested
    inner class HapticFeedbackToggle {
        @Test
        fun `GIVEN haptic enabled WHEN toggled off THEN setting is updated`() = runTest {
            createViewModel()

            underTest.onHapticFeedbackToggled(false)
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(underTest.uiState.value.isHapticFeedbackEnabled)
            assertFalse(appSettingsRepository.getCurrentSettings().enableVibrations)
        }

        @Test
        fun `GIVEN haptic disabled WHEN toggled on THEN setting is updated`() = runTest {
            appSettingsRepository.updateEnableVibrations(false)
            createViewModel()

            underTest.onHapticFeedbackToggled(true)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(underTest.uiState.value.isHapticFeedbackEnabled)
            assertTrue(appSettingsRepository.getCurrentSettings().enableVibrations)
        }
    }

    @Nested
    inner class AutoPromoteToggle {
        @Test
        fun `GIVEN auto-promote enabled WHEN toggled off THEN setting is updated`() = runTest {
            createViewModel()

            underTest.onAutoPromoteToggled(false)
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(underTest.uiState.value.isAutoPromoteEnabled)
            assertFalse(appSettingsRepository.getCurrentSettings().autoPromote)
        }

        @Test
        fun `GIVEN auto-promote disabled WHEN toggled on THEN setting is updated`() = runTest {
            appSettingsRepository.updateAutoPromote(false)
            createViewModel()

            underTest.onAutoPromoteToggled(true)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(underTest.uiState.value.isAutoPromoteEnabled)
            assertTrue(appSettingsRepository.getCurrentSettings().autoPromote)
        }
    }

    @Nested
    inner class ShowBordersToggle {
        @Test
        fun `GIVEN borders shown WHEN toggled off THEN setting is updated`() = runTest {
            createViewModel()

            underTest.onShowBordersToggled(false)
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(underTest.uiState.value.isShowBordersEnabled)
            assertFalse(appSettingsRepository.getCurrentSettings().showBorders)
        }

        @Test
        fun `GIVEN borders hidden WHEN toggled on THEN setting is updated`() = runTest {
            appSettingsRepository.updateShowBorders(false)
            createViewModel()

            underTest.onShowBordersToggled(true)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(underTest.uiState.value.isShowBordersEnabled)
            assertTrue(appSettingsRepository.getCurrentSettings().showBorders)
        }
    }

    @Nested
    inner class HighlightLegalMovesToggle {
        @Test
        fun `GIVEN highlights enabled WHEN toggled off THEN setting is updated`() = runTest {
            createViewModel()

            underTest.onHighlightLegalMovesToggled(false)
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(underTest.uiState.value.isHighlightLegalMovesEnabled)
            assertFalse(appSettingsRepository.getCurrentSettings().highlightLegalMoves)
        }

        @Test
        fun `GIVEN highlights disabled WHEN toggled on THEN setting is updated`() = runTest {
            appSettingsRepository.updateHighlightLegalMoves(false)
            createViewModel()

            underTest.onHighlightLegalMovesToggled(true)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(underTest.uiState.value.isHighlightLegalMovesEnabled)
            assertTrue(appSettingsRepository.getCurrentSettings().highlightLegalMoves)
        }
    }

    @Nested
    inner class LightModeSelection {
        @Test
        fun `GIVEN system mode WHEN light selected THEN setting is updated`() = runTest {
            createViewModel()

            underTest.onLightModeSelected(AppSettings.LightMode.Light)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AppSettings.LightMode.Light, underTest.uiState.value.lightMode)
            assertEquals(AppSettings.LightMode.Light, appSettingsRepository.getCurrentSettings().lightMode)
        }

        @Test
        fun `GIVEN system mode WHEN dark selected THEN setting is updated`() = runTest {
            createViewModel()

            underTest.onLightModeSelected(AppSettings.LightMode.Dark)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AppSettings.LightMode.Dark, underTest.uiState.value.lightMode)
            assertEquals(AppSettings.LightMode.Dark, appSettingsRepository.getCurrentSettings().lightMode)
        }

        @Test
        fun `GIVEN dark mode WHEN system selected THEN setting is updated`() = runTest {
            appSettingsRepository.updateLightMode(AppSettings.LightMode.Dark)
            createViewModel()

            underTest.onLightModeSelected(AppSettings.LightMode.System)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AppSettings.LightMode.System, underTest.uiState.value.lightMode)
            assertEquals(AppSettings.LightMode.System, appSettingsRepository.getCurrentSettings().lightMode)
        }
    }

    @Nested
    inner class AnimationsToggle {
        @Test
        fun `GIVEN animations enabled WHEN toggled off THEN setting is updated`() = runTest {
            createViewModel()

            underTest.onAnimationsToggled(false)
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(underTest.uiState.value.isAnimationsEnabled)
            assertFalse(appSettingsRepository.getCurrentSettings().enableAnimations)
        }

        @Test
        fun `GIVEN animations disabled WHEN toggled on THEN setting is updated`() = runTest {
            appSettingsRepository.updateEnableAnimations(false)
            createViewModel()

            underTest.onAnimationsToggled(true)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(underTest.uiState.value.isAnimationsEnabled)
            assertTrue(appSettingsRepository.getCurrentSettings().enableAnimations)
        }
    }

    private fun createViewModel() {
        underTest = SettingsViewModel(appSettingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
