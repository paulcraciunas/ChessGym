package com.paulcraciunas.puzzles.impl.network.save

import java.io.File

interface PuzzleDatabaseWriter {
    suspend fun writePuzzlesToDatabase(csvSource: File)
}
