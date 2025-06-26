package com.paulcraciunas.global.device.api.fakes

import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace

class FakeGetFreeDiskSpace : GetFreeDiskSpace {
    private var currentDiskSpace = GetFreeDiskSpace.DiskSpace(
        freeBytes = 2_000_000_000L,
        totalBytes = 4_000_000_000L
    )

    fun setDiskSpace(diskSpace: GetFreeDiskSpace.DiskSpace) {
        currentDiskSpace = diskSpace
    }

    override fun invoke(): GetFreeDiskSpace.DiskSpace = currentDiskSpace
}
