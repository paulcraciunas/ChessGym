package com.paulcraciunas.tools.puzzles.generate

import kotlinx.coroutines.runBlocking
import java.io.File

private suspend fun generate(inputFolderPath: String, outputFolderPath: String) {
    val inputFolder = File(inputFolderPath)
    val outputFolder = File(outputFolderPath)

    // Check if the input folder exists
    if (!inputFolder.exists() || !inputFolder.isDirectory) {
        println("Error: Input folder does not exist or is not a directory.")
        return
    }

    // Create the output folder if it doesn't exist
    if (!outputFolder.exists()) {
        if (!outputFolder.mkdirs()) {
            println("Error: Failed to create output folder at: ${outputFolder.absolutePath}")
            return
        }
    }
    Database(input = inputFolder, output = outputFolder).generate()
}

class Generate {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val inputFolderPath = args.find { it.startsWith(IN) }?.substringAfter(IN)
            val outputFolderPath = args.find { it.startsWith(OUT) }?.substringAfter(OUT)

            if (inputFolderPath == null || outputFolderPath == null) {
                println("Usage: $IN<input folder path> $OUT<output folder path>")
                return
            }

            runBlocking {
                generate(inputFolderPath, outputFolderPath)
            }
        }
    }
}

private const val IN = "--in="
private const val OUT = "--out="
