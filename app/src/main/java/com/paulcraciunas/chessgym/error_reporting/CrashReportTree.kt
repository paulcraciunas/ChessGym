package com.paulcraciunas.chessgym.error_reporting

import android.util.Log
import com.paulcraciunas.chessgym.BuildConfig
import timber.log.Timber

/**
 * Timber tree for release builds that forwards warnings and errors to [android.util.Log].
 * Debug and info logs are stripped in release.
 */
internal class CrashReportTree(
    private val minPriority: Int = Log.WARN,
) : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= minPriority

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val finalTag = tag ?: DEFAULT_TAG

        // 1. Local logging (Optional: some prefer to disable Logcat entirely in release)
        if (t != null) {
            Log.println(priority, finalTag, "$message\n${Log.getStackTraceString(t)}")
        } else {
            Log.println(priority, finalTag, message)
        }

        // 2. External reporting (e.g., FirebaseCrashlytics or Sentry)
        // Format: [TAG] Message
        // val formattedMessage = "[$finalTag] $message"
        // reportToExternalService(priority, finalTag, formattedMessage, t)
    }

    // TODO(https://github.com/paulcraciunas/ChessGym/issues/28) Paul: Integrate Crashlytics
//    private fun reportToExternalService(priority: Int, message: String, t: Throwable?) {
//        // Placeholder for actual Crashlytics/Sentry calls
//        Crashlytics.setCustomKey("last_puzzle_id", ...)
//        Crashlytics.getInstance().log("$message")
//        if (t != null) Crashlytics.getInstance().recordException(t)
//    }

    companion object {
        private const val DEFAULT_TAG = "ChessGym"

        fun tree(): Timber.Tree =
            if (BuildConfig.DEBUG) {
                Timber.DebugTree()
            } else {
                CrashReportTree()
            }
    }
}
