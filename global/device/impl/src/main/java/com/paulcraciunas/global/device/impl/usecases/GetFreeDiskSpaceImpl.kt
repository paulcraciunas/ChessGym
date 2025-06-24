package com.paulcraciunas.global.device.impl.usecases

import android.content.Context
import android.os.StatFs
import com.paulcraciunas.global.device.api.usecases.GetFreeDiskSpace
import javax.inject.Inject

class GetFreeDiskSpaceImpl @Inject constructor(
    private val context: Context
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
            // Return a safe fallback if we can't get disk space info
            GetFreeDiskSpace.DiskSpace(
                freeBytes = 0L,
                totalBytes = 0L
            )
        }
    }
} 