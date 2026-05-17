package com.paulcraciunas.puzzles.impl.network.fakes

import com.paulcraciunas.puzzles.impl.network.source.PuzzleDatabaseSource
import java.io.ByteArrayInputStream
import java.io.InputStream

internal class FakeDatabaseSource : Failable(), PuzzleDatabaseSource {
    var testDbContent: ByteArray = ByteArray(64)
    var configuredTier: String? = null

    override suspend fun open(tierSegment: String): Long {
        configuredTier = tierSegment
        check()
        return testDbContent.size.toLong()
    }

    override suspend fun read(): InputStream {
        check()
        return ByteArrayInputStream(testDbContent)
    }
}
