package com.paulcraciunas.chessgym.error_reporting

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.paulcraciunas.game.logic.api.diagnostics.LastLoadedPuzzleLog
import com.paulcraciunas.game.logic.api.diagnostics.PlayedMovesLog
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

internal class GlobalExceptionHandler : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    // Atomic to handle simultaneous crashes from different threads safely
    private val isCrashing = AtomicBoolean(false)

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Prevent recursive crashes from logging logic
            if (isCrashing.compareAndSet(false, true)) {
                logDetailedCrash(thread, throwable)
            }
        } catch (e: Throwable) {
            // Fallback to basic error output if enrichment fails
            System.err.println("Failed to log enriched crash report: ${e.message}")
        } finally {
            // Always pass the exception to the OS/Default handler to ensure process termination
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun logDetailedCrash(thread: Thread, throwable: Throwable) {
        val crashlytics = runCatching { FirebaseCrashlytics.getInstance() }.getOrNull() ?: return
        val puzzleSummary = runCatching { LastLoadedPuzzleLog.summary() }.getOrDefault("Unavailable")
        val movesSummary = runCatching { PlayedMovesLog.summary() }.getOrDefault("Unavailable")

        crashlytics.setCustomKey("crash_thread", thread.name)
        crashlytics.setCustomKey("last_puzzle", puzzleSummary.take(1024))
        crashlytics.setCustomKey("played_moves", movesSummary.take(1024))

        Timber.e(
            throwable,
            "FATAL CRASH on thread [%s]\nLast loaded puzzle: %s\nPlayed moves: %s",
            thread.name,
            puzzleSummary,
            movesSummary,
        )
    }
}
