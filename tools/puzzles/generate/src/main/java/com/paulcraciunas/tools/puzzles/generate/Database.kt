package com.paulcraciunas.tools.puzzles.generate

import com.paulcraciunas.game.io.FenBinarySerializer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.FileOutputStream

internal class Database(
    private val input: File,
    private val output: File,
) {
    private var progressUpdate = ""
    private val validator = Validator()
    private val serializer = FenBinarySerializer()

    suspend fun generate() {
        println("Generating binary outputs...")
        val inputFiles = input.listFiles()?.filter { !it.isHidden && it.extension == "csv" } ?: error("Can't read from $input")
        val inputSize = inputFiles.size
        val binaryFailures = mutableListOf<String>()
        val testFailures = mutableListOf<String>()

        print("Progress: ")
        var progress = 0
        val chunk = Runtime.getRuntime().availableProcessors()
        inputFiles.chunked(chunk).forEach { inputs ->
            coroutineScope {
                inputs.map { input ->
                    async {
                        val outputFile = File(output, "${input.nameWithoutExtension}.bin")
                        input.useLines { lines ->
                            var allBytes = ByteArray(0)
                            FileOutputStream(outputFile).use { writer ->
                                for (line in lines) {
                                    val binary = serializer.toBinary(line)
                                    allBytes += binary
                                    allBytes += "\n".toByteArray()
                                    if (!validate(binary, line)) {
                                        binaryFailures.add(line)
                                    }
                                    if (!validator.test(binary, line)) {
                                        testFailures.add(line)
                                    }
                                }
                                writer.write(allBytes)
                            }
                        }
                    }
                }.awaitAll()
            }
            progress += chunk
            printProgress(progress, inputSize)
        }
        println()
        printResults(binaryFailures, testFailures)
    }

    private fun validate(binary: ByteArray, original: String): Boolean =
        serializer.fromBinary(binary) == original

    private fun printProgress(idx: Int, inputSize: Int) {
        progressUpdate.chars().forEach { // Delete old progress update
            print(BACK)
        }
        progressUpdate = "%.2f".format(((idx + 1).toFloat() / inputSize) * 100) + "%"
        print(progressUpdate)
    }

    private fun printResults(
        binaryFailures: MutableList<String>,
        testFailures: MutableList<String>
    ) {
        println("Binary generation done.")
        if (binaryFailures.isEmpty() && testFailures.isEmpty()) {
            println("No errors found! Well done!")
        } else {
            if (binaryFailures.isNotEmpty()) {
                binaryFailures.printTo("BinaryFailures.dat")
                println("Found errors when converting puzzles to binary. They can be found in BinaryFailures.dat")
            }
            if (testFailures.isNotEmpty()) {
                testFailures.printTo("TestFailures.dat")
                println("Found errors when testing binary puzzles. They can be found in TestFailures.dat")
            }
        }
    }

    private fun MutableList<String>.printTo(fileName: String) {
        File(output, fileName).printWriter().use { writer ->
            forEach {
                writer.println(it)
            }
        }
    }

    companion object {
        private const val BACK = "\b"
    }
}
