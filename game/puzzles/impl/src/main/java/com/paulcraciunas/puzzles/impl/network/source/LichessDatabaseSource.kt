package com.paulcraciunas.puzzles.impl.network.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import javax.inject.Inject

internal class LichessDatabaseSource @Inject constructor() : PuzzleDatabaseSource {
    private lateinit var connection: URLConnection

    override suspend fun open(): Long = withContext(Dispatchers.IO) {
        connection = URL(LICHESS_URL).openConnection()
        return@withContext connection.contentLength.toLong()
    }

    override suspend fun read(): InputStream {
        assert(::connection.isInitialized)
        return withContext(Dispatchers.IO) { connection.getInputStream() }
    }

    companion object {
        private const val LICHESS_URL = "https://database.lichess.org/lichess_db_puzzle.csv.zst"
    }
}
