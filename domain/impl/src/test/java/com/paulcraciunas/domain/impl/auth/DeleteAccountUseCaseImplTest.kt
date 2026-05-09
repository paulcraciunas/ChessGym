package com.paulcraciunas.domain.impl.auth

import com.paulcraciunas.domain.api.auth.DeleteAccountResult
import com.paulcraciunas.global.device.api.fakes.FakeGetNetworkState
import com.paulcraciunas.global.device.api.usecases.GetNetworkState.NetworkState
import com.paulcraciunas.user.api.AuthException
import com.paulcraciunas.user.api.FakeAuthService
import com.paulcraciunas.user.api.FakeSyncScheduler
import com.paulcraciunas.user.api.FakeSyncState
import com.paulcraciunas.user.api.FakeTokenProvider
import com.paulcraciunas.user.api.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DeleteAccountUseCaseImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val fakeAuthService = FakeAuthService()
    private val fakeTokenProvider = FakeTokenProvider()
    private val fakeSyncState = FakeSyncState()
    private val fakeSyncScheduler = FakeSyncScheduler()
    private val fakeGetNetworkState = FakeGetNetworkState()

    private val underTest = DeleteAccountUseCaseImpl(
        userRepository = fakeUserRepository,
        authService = fakeAuthService,
        tokenProvider = fakeTokenProvider,
        syncState = fakeSyncState,
        syncScheduler = fakeSyncScheduler,
        getNetworkState = fakeGetNetworkState,
    )

    @Test
    fun `GIVEN no network WHEN invoke THEN returns NoNetwork`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Disconnected)

        val result = underTest()

        assertEquals(DeleteAccountResult.NoNetwork, result)
    }

    @Test
    fun `GIVEN no network WHEN invoke THEN does not clear any data`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Disconnected)

        underTest()

        assertFalse(fakeTokenProvider.isSignedOut)
        assertFalse(fakeSyncState.isCleared())
        assertEquals(0, fakeSyncScheduler.cancelCount)
        assertFalse(fakeAuthService.isAccountDeleted)
    }

    @Test
    fun `GIVEN connected WHEN invoke THEN cancels sync work`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Connected)

        underTest()

        assertEquals(1, fakeSyncScheduler.cancelCount)
    }

    @Test
    fun `GIVEN connected WHEN invoke THEN clears user repository`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Connected)

        underTest()

        assertTrue(fakeUserRepository.local.isCleared)
    }

    @Test
    fun `GIVEN connected WHEN invoke THEN deletes auth account`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Connected)

        underTest()

        assertTrue(fakeAuthService.isAccountDeleted)
    }

    @Test
    fun `GIVEN connected WHEN invoke THEN clears sync state`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Connected)
        fakeSyncState.markDirty()

        underTest()

        assertTrue(fakeSyncState.isCleared())
    }

    @Test
    fun `GIVEN connected WHEN invoke THEN signs out token provider`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Connected)

        underTest()

        assertTrue(fakeTokenProvider.isSignedOut)
    }

    @Test
    fun `GIVEN connected WHEN invoke THEN returns Success`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Connected)

        val result = underTest()

        assertEquals(DeleteAccountResult.Success, result)
    }

    @Test
    fun `GIVEN auth service throws WHEN invoke THEN returns Failure with cause`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Connected)
        fakeAuthService.disconnect()

        val result = underTest()

        assertInstanceOf(DeleteAccountResult.Failure::class.java, result)
        assertInstanceOf(AuthException.NetworkError::class.java, (result as DeleteAccountResult.Failure).cause)
    }

    @Test
    fun `GIVEN unknown network state WHEN invoke THEN proceeds with deletion`() = runTest {
        fakeGetNetworkState.setState(NetworkState.Unknown)

        val result = underTest()

        assertEquals(DeleteAccountResult.Success, result)
    }
}
