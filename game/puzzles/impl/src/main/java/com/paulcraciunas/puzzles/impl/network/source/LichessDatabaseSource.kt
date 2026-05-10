package com.paulcraciunas.puzzles.impl.network.source

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

    override suspend fun open(): Long = withContext(ioDispatcher) {
        connection = URL(LICHESS_URL).openConnection()
        return@withContext connection.contentLength.toLong()
    }

    override suspend fun read(): InputStream {
        assert(::connection.isInitialized)
        return withContext(ioDispatcher) { connection.getInputStream() }
    }

    companion object {
        private const val LICHESS_URL = "https://database.lichess.org/lichess_db_puzzle.csv.zst"
    }
}
