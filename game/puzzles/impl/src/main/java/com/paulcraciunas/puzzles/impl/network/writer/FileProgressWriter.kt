package com.paulcraciunas.puzzles.impl.network.writer

import com.paulcraciunas.puzzles.impl.network.progress.ProgressReporter
import com.paulcraciunas.utils.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class FileProgressWriter @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val progressReporter: ProgressReporter
) : FileWriter {
    private val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

    override fun onBegin(size: Long) {
        progressReporter.onBegin(size)
    }

    override suspend fun write(inputStream: InputStream, destination: File) {
        withContext(ioDispatcher) {
            inputStream.use { input ->
                destination.outputStream().use { output ->
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        progressReporter.onCompleted(bytes)
                        bytes = input.read(buffer)
                    }
                }
            }
            progressReporter.onCompleted(Int.MAX_VALUE)
        }
    }
}
