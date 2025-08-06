package com.paulcraciunas.screens.loading.vm

import com.paulcraciunas.global.device.api.fakes.FakeGetFreeDiskSpace
import com.paulcraciunas.global.device.api.fakes.FakeGetNetworkState
import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class LoadingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeGetNetworkState = FakeGetNetworkState()
    private val fakeGetFreeDiskSpace = FakeGetFreeDiskSpace()
    private val fakeFetchPuzzleDatabase = FakeFetchPuzzleDatabase()

    private lateinit var underTest: LoadingViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun given_viewModelCreated_WHEN_init_THEN_stateIsReady() = runTest {
        // When
        underTest = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(LoadingState.ready(), underTest.uiState.value)
    }

    @Test
    fun given_readyStateWithNoInternet_WHEN_onDownloadAfterConfirmationAndPermission_THEN_showsNoInternetError() = runTest {
        // Given
        fakeGetNetworkState.setState(GetNetworkState.NetworkState.Disconnected)
        underTest = createViewModel()
        advanceUntilIdle()

        // When
        underTest.onDownloadConfirmation(true)
        underTest.onPermissionReceived(true)
        advanceUntilIdle()

        // Then
        val expectedState = LoadingState.Ready(
            requiresConfirmation = false,
            requiresPermission = false,
            dialog = LoadingState.Dialog.None,
            error = LoadingState.Error.NoInternet
        )
        assertEquals(expectedState, underTest.uiState.value)
    }

    @Test
    fun given_readyStateWithInsufficientDiskSpace_WHEN_onDownloadAfterConfirmationAndPermission_THEN_showsNotEnoughDiskSpaceError() =
        runTest {
            // Given
            fakeGetNetworkState.setState(GetNetworkState.NetworkState.Connected)
            fakeGetFreeDiskSpace.setDiskSpace(
                GetFreeDiskSpace.DiskSpace(
                    freeBytes = 500_000_000L, // Less than required 1.8GB
                    totalBytes = 2_000_000_000L
                )
            )
            underTest = createViewModel()
            advanceUntilIdle()

            // When
            underTest.onDownloadConfirmation(true)
            underTest.onPermissionReceived(true)
            advanceUntilIdle()

            // Then
            val expectedState = LoadingState.Ready(
                requiresConfirmation = false,
                requiresPermission = false,
                dialog = LoadingState.Dialog.None,
                error = LoadingState.Error.NotEnoughDiskSpace
            )
            assertEquals(expectedState, underTest.uiState.value)
        }

    @Test
    fun given_readyStateWithGoodConditions_WHEN_onDownloadAfterConfirmationAndPermission_THEN_startDownloading() = runTest {
        // Given
        fakeGetNetworkState.setState(GetNetworkState.NetworkState.Connected)
        fakeGetFreeDiskSpace.setDiskSpace(
            GetFreeDiskSpace.DiskSpace(
                freeBytes = 2_000_000_000L, // More than required 1.8GB
                totalBytes = 4_000_000_000L
            )
        )
        underTest = createViewModel()
        advanceUntilIdle()

        // When
        underTest.onDownloadConfirmation(true)
        underTest.onPermissionReceived(true)
        advanceUntilIdle()

        // Then - should be Complete since fake returns 100% progress immediately
        assertEquals(LoadingState.Complete, underTest.uiState.value)
    }

    @Test
    fun given_errorState_WHEN_onRetry_THEN_retriesDeviceConditionsCheck() = runTest {
        // Given
        fakeGetNetworkState.setState(GetNetworkState.NetworkState.Disconnected)
        underTest = createViewModel()
        advanceUntilIdle()

        underTest.onDownloadConfirmation(true)
        underTest.onPermissionReceived(true)
        advanceUntilIdle()

        // Verify error state
        val errorState = underTest.uiState.value as LoadingState.Ready
        assertEquals(LoadingState.Error.NoInternet, errorState.error)

        // When - fix network and retry
        fakeGetNetworkState.setState(GetNetworkState.NetworkState.Connected)
        fakeGetFreeDiskSpace.setDiskSpace(
            GetFreeDiskSpace.DiskSpace(
                freeBytes = 2_000_000_000L,
                totalBytes = 4_000_000_000L
            )
        )
        underTest.onDownload()
        advanceUntilIdle()

        // Then - should be Complete since fake returns 100% progress immediately
        assertEquals(LoadingState.Complete, underTest.uiState.value)
    }

    @Test
    fun given_noInternetError_WHEN_networkRecovers_THEN_errorAutomaticallyClears() = runTest {
        // Given - start with no internet
        fakeGetNetworkState.setState(GetNetworkState.NetworkState.Disconnected)
        underTest = createViewModel()
        advanceUntilIdle()

        // Trigger the error state
        underTest.onDownloadConfirmation(true)
        underTest.onPermissionReceived(true)
        advanceUntilIdle()

        // Verify error state
        val errorState = underTest.uiState.value as LoadingState.Ready
        assertEquals(LoadingState.Error.NoInternet, errorState.error)

        // When - network recovers automatically
        fakeGetNetworkState.setState(GetNetworkState.NetworkState.Connected)
        advanceUntilIdle()

        // Then - error should be automatically cleared and download should start
        assertEquals(LoadingState.Complete, underTest.uiState.value)
    }

    private fun createViewModel() = LoadingViewModel(
        getNetworkState = fakeGetNetworkState,
        getFreeDiskSpace = fakeGetFreeDiskSpace,
        fetchPuzzleDatabase = fakeFetchPuzzleDatabase,
    )
}

private class FakeFetchPuzzleDatabase : FetchPuzzleDatabase {
    override fun invoke() = flowOf(
        FetchPuzzleDatabase.Progress(download = 100, unpack = 100, buildDb = 100)
    )
}
