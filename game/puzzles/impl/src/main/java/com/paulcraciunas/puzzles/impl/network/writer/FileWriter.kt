package com.paulcraciunas.puzzles.impl.network.writer

import java.io.File
import java.io.InputStream

interface FileWriter {
    fun onBegin(size: Long)

    suspend fun write(inputStream: InputStream, destination: File)
}
