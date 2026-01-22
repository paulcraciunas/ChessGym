package com.paulcraciunas.domain.api

/**
 * Use case for handling successful puzzle completion in streak mode.
 *
 * This use case is responsible for:
 * - Incrementing the current streak count
 */
interface OnStreakPuzzleComplete {
    /**
     * Processes a successfully completed puzzle in streak mode.
     */
    suspend operator fun invoke()
}
