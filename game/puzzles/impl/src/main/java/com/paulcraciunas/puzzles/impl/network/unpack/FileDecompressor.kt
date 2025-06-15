package com.paulcraciunas.puzzles.impl.network.unpack

import java.io.InputStream

interface FileDecompressor {
    suspend fun decompress(source: InputStream): InputStream
}
