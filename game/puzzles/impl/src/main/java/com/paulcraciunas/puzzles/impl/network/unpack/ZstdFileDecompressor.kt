package com.paulcraciunas.puzzles.impl.network.unpack

import com.github.luben.zstd.ZstdInputStream
import java.io.InputStream
import javax.inject.Inject

class ZstdFileDecompressor @Inject constructor() : FileDecompressor {
    override suspend fun decompress(source: InputStream): InputStream = ZstdInputStream(source)
}
