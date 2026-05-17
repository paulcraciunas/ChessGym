package com.paulcraciunas.puzzles.impl.network.source

import com.paulcraciunas.puzzles.api.PuzzleDatabaseContract
import com.paulcraciunas.utils.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import javax.inject.Inject

class LichessDatabaseSource @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PuzzleDatabaseSource {
    private lateinit var connection: URLConnection

    override suspend fun open(tierSegment: String): Long = withContext(ioDispatcher) {
        connection = URL(buildDownloadUrl(tierSegment)).openConnection()
        return@withContext connection.contentLength.toLong()
    }

    override suspend fun read(): InputStream {
        assert(::connection.isInitialized)
        return withContext(ioDispatcher) { connection.getInputStream() }
    }

    companion object {
        // The name "puzzle-db-v1" is also set in the "build-puzzle-db.yml" build pipeline.
        // CAUTION: if you change this, also update the aforementioned CI build
        private const val BASE_URL = "https://github.com/paulcraciunas/ChessGym/releases/download/puzzle-db-v1"

        private fun buildDownloadUrl(tierSegment: String): String =
            "$BASE_URL/${PuzzleDatabaseContract.compressedFileName(tierSegment)}"
    }
}
