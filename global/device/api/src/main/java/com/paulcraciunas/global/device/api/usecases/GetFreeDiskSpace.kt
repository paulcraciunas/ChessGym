package com.paulcraciunas.global.device.api.usecases

interface GetFreeDiskSpace {
    operator fun invoke(): DiskSpace

    data class DiskSpace(
        val freeBytes: Long,
        val totalBytes: Long
    ) {
        val usedBytes: Long get() = totalBytes - freeBytes
        val freePercentage: Double get() = if (totalBytes > 0) (freeBytes.toDouble() / totalBytes.toDouble()) * 100.0 else 0.0
        val usedPercentage: Double get() = if (totalBytes > 0) (usedBytes.toDouble() / totalBytes.toDouble()) * 100.0 else 0.0
        
        fun hasEnoughSpace(requiredBytes: Long): Boolean = freeBytes >= requiredBytes
    }
}
