package com.paulcraciunas.domain.impl.auth

import com.paulcraciunas.user.api.FakeSyncScheduler
import com.paulcraciunas.user.api.FakeSyncState
import com.paulcraciunas.user.api.FakeTokenProvider
import com.paulcraciunas.user.api.FakeUserRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SignOutUseCaseImplTest {
    private val fakeUserRepository = FakeUserRepository()
    private val fakeTokenProvider = FakeTokenProvider()
    private val fakeSyncState = FakeSyncState()
    private val fakeSyncScheduler = FakeSyncScheduler()

    private val underTest = SignOutUseCaseImpl(
        userRepository = fakeUserRepository,
        tokenProvider = fakeTokenProvider,
        syncState = fakeSyncState,
        syncScheduler = fakeSyncScheduler,
    )

    @Test
    fun `GIVEN user signed in WHEN invoke THEN cancels scheduled sync`() = runTest {
        underTest()

        assertEquals(1, fakeSyncScheduler.cancelCount)
    }

    @Test
    fun `GIVEN user signed in WHEN invoke THEN clears sync state`() = runTest {
        fakeSyncState.markDirty()

        underTest()

        assertTrue(fakeSyncState.isCleared())
    }

    @Test
    fun `GIVEN user signed in WHEN invoke THEN signs out token provider`() = runTest {
        underTest()

        assertTrue(fakeTokenProvider.isSignedOut)
    }

    @Test
    fun `GIVEN user signed in WHEN invoke THEN clears user repository`() = runTest {
        underTest()

        assertTrue(fakeUserRepository.local.isCleared)
    }

    @Test
    fun `GIVEN user signed in WHEN invoke THEN executes in correct order`() = runTest {
        fakeSyncState.markDirty()

        underTest()

        assertEquals(1, fakeSyncScheduler.cancelCount)
        assertTrue(fakeSyncState.isCleared())
        assertTrue(fakeTokenProvider.isSignedOut)
        assertTrue(fakeUserRepository.local.isCleared)
    }
}
