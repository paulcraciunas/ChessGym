package com.paulcraciunas.puzzles.impl.network.fakes

import com.paulcraciunas.puzzles.impl.network.unpack.FileDecompressor
import java.io.InputStream

internal class FakeDecompressor : Failable(), FileDecompressor {
    override suspend fun decompress(source: InputStream): InputStream {
        check()
        return source
    }
}
