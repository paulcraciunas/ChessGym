package com.paulcraciunas.tools.puzzles.sanitize

/**
 * This should be able to take a lichess database and "sanitize" it.
 * That is to say, the output should be a folder of files, where each file contains all
 * the puzzles of a certain rating.
 * E.g.
 * ```
 * kotlin Sanitize.kt -db lichessDb.tar.gz -into /puzzles
 * ```
 * would produce
 * puzzles/
 *  - 399.csv
 *  - 400.csv
 *  - ...
 *
 *  Use the scripts in the scripts/ folder for this
 *  When done, document all this in a Readme.md file
 */
fun main(args: Array<String>) {
    TODO("Paul: Implement me")
}
