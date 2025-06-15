package com.paulcraciunas.puzzles.impl.network.source

import java.io.InputStream

interface PuzzleDatabaseSource {
    suspend fun open(): Long
    suspend fun read(): InputStream
}
