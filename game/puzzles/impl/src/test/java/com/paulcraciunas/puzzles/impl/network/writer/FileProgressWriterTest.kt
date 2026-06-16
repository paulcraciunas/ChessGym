package com.paulcraciunas.puzzles.impl.network.writer

import com.paulcraciunas.puzzles.impl.network.fakes.FailingInputStream
import com.paulcraciunas.puzzles.impl.network.fakes.FakeInputStream
import com.paulcraciunas.puzzles.impl.network.progress.WorkerProgressReporter
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FileProgressWriterTest {
    private val ioDispatcher = StandardTestDispatcher()
    private var result = 0
    private val reporter = WorkerProgressReporter().apply { init { result = it } }

    private val underTest = FileProgressWriter(ioDispatcher, reporter)

    @Test
    fun `GIVEN valid input stream and destination WHEN write is called THEN file is downloaded with progress updates`(@TempDir tempDir: File) =
        runTest(ioDispatcher) {
            // Given
            val testContent = "test content for write"
            val destination = File(tempDir, "test.txt")
            val inputStream = FakeInputStream(testContent.toByteArray())

            // When
            underTest.write(inputStream, destination)

            // Then
            assertTrue(destination.exists())
            assertEquals(testContent, destination.readText())
            assertEquals(100, result)
        }

    @Test
    fun `GIVEN empty input stream WHEN write is called THEN empty file is created`(@TempDir tempDir: File) = runTest(ioDispatcher) {
        // Given
        val destination = File(tempDir, "empty.txt")
        val inputStream = FakeInputStream(byteArrayOf())

        // When
        underTest.write(inputStream, destination)

        // Then
        assertTrue(destination.exists())
        assertEquals("", destination.readText())
        assertEquals(100, result)
    }

    @Test
    fun `GIVEN failing input stream WHEN write is called THEN exception is propagated`(@TempDir tempDir: File) = runTest(ioDispatcher) {
        // Given
        val destination = File(tempDir, "test.txt")
        val inputStream = FailingInputStream()

        // When & Then
        assertThrows(RuntimeException::class.java) {
            runTest(ioDispatcher) {
                underTest.write(inputStream, destination)
            }
        }
    }

    @Test
    fun `GIVEN large file WHEN write is called THEN progress is reported correctly`(@TempDir tempDir: File) = runTest(ioDispatcher) {
        // Given
        val largeContent = "x".repeat(10000) // 10KB file
        val destination = File(tempDir, "large.txt")
        val inputStream = FakeInputStream(largeContent.toByteArray())

        // When
        underTest.write(inputStream, destination)

        // Then
        assertTrue(destination.exists())
        assertEquals(largeContent, destination.readText())
        assertEquals(100, result)
    }
}
