package com.paulcraciunas.screens.loading.vm

import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace
import com.paulcraciunas.global.device.api.usecases.GetNetworkState
import com.paulcraciunas.puzzles.api.usecases.FetchPuzzleDatabase
import com.paulcraciunas.settings.testutils.FakeAppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
internal class LoadingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Test fakes
    private val fakeAppSettingsRepository = FakeAppSettingsRepository()
    private val fakeGetNetworkState = FakeGetNetworkState()
    private val fakeGetFreeDiskSpace = FakeGetFreeDiskSpace()
    private val fakeFetchPuzzleDatabase = FakeFetchPuzzleDatabase()

    private lateinit var underTest: LoadingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun given_puzzlesAlreadyDownloaded_WHEN_init_THEN_stateIsComplete() = runTest {
        // Given
        fakeAppSettingsRepository.updatePuzzlesDownloaded(true)

        // When
        underTest = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(LoadingState.Complete, underTest.uiState.value)
    }

    @Test
    fun given_puzzlesNotDownloaded_WHEN_init_THEN_stateIsReady() = runTest {
        // Given
        fakeAppSettingsRepository.updatePuzzlesDownloaded(false)

        // When
        underTest = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(LoadingState.ready(), underTest.uiState.value)
    }

    @Test
    fun given_readyStateWithNoInternet_WHEN_onDownloadAfterConfirmationAndPermission_THEN_showsNoInternetError() = runTest {
        // Given
        fakeAppSettingsRepository.updatePuzzlesDownloaded(false)
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
            fakeAppSettingsRepository.updatePuzzlesDownloaded(false)
            fakeGetNetworkState.setState(GetNetworkState.NetworkState.Connected)
            fakeGetFreeDiskSpace.setDiskSpace(
                GetFreeDiskSpace.DiskSpace(
                    freeBytes = 500_000_000L, // Less than required 1.5GB
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
        fakeAppSettingsRepository.updatePuzzlesDownloaded(false)
        fakeGetNetworkState.setState(GetNetworkState.NetworkState.Connected)
        fakeGetFreeDiskSpace.setDiskSpace(
            GetFreeDiskSpace.DiskSpace(
                freeBytes = 2_000_000_000L, // More than required 1.5GB
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
        fakeAppSettingsRepository.updatePuzzlesDownloaded(false)
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
        underTest.onRetry()
        advanceUntilIdle()

        // Then - should be Complete since fake returns 100% progress immediately
        assertEquals(LoadingState.Complete, underTest.uiState.value)
    }

    private fun createViewModel() = LoadingViewModel(
        appSettingsRepository = fakeAppSettingsRepository,
        getNetworkState = fakeGetNetworkState,
        getFreeDiskSpace = fakeGetFreeDiskSpace,
        fetchPuzzleDatabase = fakeFetchPuzzleDatabase
    )
}

private class FakeGetNetworkState : GetNetworkState {
    private var currentState: GetNetworkState.NetworkState = GetNetworkState.NetworkState.Connected

    fun setState(state: GetNetworkState.NetworkState) {
        currentState = state
    }

    override fun invoke(): GetNetworkState.NetworkState = currentState
}

private class FakeGetFreeDiskSpace : GetFreeDiskSpace {
    private var currentDiskSpace = GetFreeDiskSpace.DiskSpace(
        freeBytes = 2_000_000_000L,
        totalBytes = 4_000_000_000L
    )

    fun setDiskSpace(diskSpace: GetFreeDiskSpace.DiskSpace) {
        currentDiskSpace = diskSpace
    }

    override fun invoke(): GetFreeDiskSpace.DiskSpace = currentDiskSpace
}

private class FakeFetchPuzzleDatabase : FetchPuzzleDatabase {
    override fun invoke() = flowOf(
        FetchPuzzleDatabase.Progress(download = 100, unpack = 100, buildDb = 100)
    )
}
