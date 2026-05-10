package com.paulcraciunas.chessgym.error_reporting

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.paulcraciunas.chessgym.BuildConfig
import timber.log.Timber

/**
 * Timber tree for release builds that forwards info, warnings and errors to Firebase Crashlytics.
 * Debug and verbose logs are stripped in release.
 */
internal class CrashReportTree(
    private val minPriority: Int = Log.INFO,
) : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= minPriority

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        val finalTag = tag ?: DEFAULT_TAG
        val formattedMessage = "[$finalTag] $message"

        crashlytics.log(formattedMessage)

        if (t != null) {
            crashlytics.recordException(t)
        }
    }

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
