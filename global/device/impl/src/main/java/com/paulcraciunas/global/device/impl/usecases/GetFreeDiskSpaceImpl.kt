package com.paulcraciunas.global.device.impl.usecases

import android.content.Context
import android.os.StatFs
import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class GetFreeDiskSpaceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GetFreeDiskSpace {

    override operator fun invoke(): GetFreeDiskSpace.DiskSpace {
        return try {
            val dataDir = context.filesDir
            val stat = StatFs(dataDir.path)

            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize

            GetFreeDiskSpace.DiskSpace(
                freeBytes = freeBytes,
                totalBytes = totalBytes
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed to read disk space info")
            GetFreeDiskSpace.DiskSpace(
                freeBytes = 0L,
                totalBytes = 0L
            )
        }
    }
}
