package com.paulcraciunas.puzzles.impl.network.fakes

import java.io.InputStream

// Test implementation that provides controlled data
internal class FakeInputStream(private val data: ByteArray) : InputStream() {
    private var position = 0

    override fun read(): Int {
        return if (position < data.size) {
            data[position++].toInt() and 0xFF
        } else {
            -1
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (position >= data.size) return -1

        val bytesToRead = minOf(len, data.size - position)
        System.arraycopy(data, position, b, off, bytesToRead)
        position += bytesToRead
        return bytesToRead
    }
}

// Test implementation that simulates network errors
internal class FailingInputStream : InputStream() {
    override fun read(): Int {
        throw RuntimeException("Network error")
    }
}
