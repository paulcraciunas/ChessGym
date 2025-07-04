package com.paulcraciunas.global.device.api.usecases

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetFreeDiskSpaceTest {

    @Test
    fun given_diskSpaceData_WHEN_computingUsedBytes_THEN_returnsCorrectValue() {
        // Given
        val diskSpace = GetFreeDiskSpace.DiskSpace(
            freeBytes = 300L,
            totalBytes = 1000L
        )

        // When
        val usedBytes = diskSpace.usedBytes

        // Then
        assertEquals(700L, usedBytes)
    }

    @Test
    fun given_diskSpaceData_WHEN_computingPercentages_THEN_returnsCorrectValues() {
        // Given
        val diskSpace = GetFreeDiskSpace.DiskSpace(
            freeBytes = 250L,
            totalBytes = 1000L
        )

        // When & Then
        assertEquals(25.0, diskSpace.freePercentage, 0.01)
        assertEquals(75.0, diskSpace.usedPercentage, 0.01)
    }

    @Test
    fun given_zeroTotalBytes_WHEN_computingPercentages_THEN_returnsZero() {
        // Given
        val diskSpace = GetFreeDiskSpace.DiskSpace(
            freeBytes = 0L,
            totalBytes = 0L
        )

        // When & Then
        assertEquals(0.0, diskSpace.freePercentage, 0.01)
        assertEquals(0.0, diskSpace.usedPercentage, 0.01)
    }

    @Test
    fun given_sufficientSpace_WHEN_checkingEnoughSpace_THEN_returnsTrue() {
        // Given
        val diskSpace = GetFreeDiskSpace.DiskSpace(
            freeBytes = 1000L,
            totalBytes = 2000L
        )

        // When & Then
        assertTrue(diskSpace.hasEnoughSpace(500L))
        assertTrue(diskSpace.hasEnoughSpace(1000L))
    }

    @Test
    fun given_insufficientSpace_WHEN_checkingEnoughSpace_THEN_returnsFalse() {
        // Given
        val diskSpace = GetFreeDiskSpace.DiskSpace(
            freeBytes = 500L,
            totalBytes = 2000L
        )

        // When & Then
        assertFalse(diskSpace.hasEnoughSpace(600L))
        assertFalse(diskSpace.hasEnoughSpace(1000L))
    }
}