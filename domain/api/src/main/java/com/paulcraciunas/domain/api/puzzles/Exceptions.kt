package com.paulcraciunas.domain.api.puzzles

class PuzzleGenerationException(message: String, cause: Throwable) : Exception(message, cause)

class NoPuzzleException(message: String) : Exception(message)
