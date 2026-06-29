package com.paulcraciunas.screens.loading.vm

import com.paulcraciunas.puzzles.api.PuzzleDatabaseContract

enum class DatabaseTier(
    val approximateSize: String, // Does not need to be translated
    val tierSegment: String,
    val isBundled: Boolean,
) {
    Full("~ 185 MB", PuzzleDatabaseContract.Tier.FULL, false),
    Compact("~ 76 MB", PuzzleDatabaseContract.Tier.COMPACT, false),
    Lite("0", PuzzleDatabaseContract.Tier.LITE, true);

    companion object {
        val DEFAULT: DatabaseTier = Compact
    }
}

sealed class LoadingState {
    data class Downloading(
        val progress: Progress = Progress.empty(),
        val factIndex: Int = 0,
    ) : LoadingState() {
        data class Progress(
            val download: Int,
            val unpack: Int,
        ) {
            companion object {
                fun empty(): Progress = Progress(download = 0, unpack = 0)
            }
        }
    }

    data object Complete : LoadingState()
    data class Ready(
        val selectedTier: DatabaseTier = DatabaseTier.DEFAULT,
        val requiresConfirmation: Boolean = true,
        val dialog: Dialog = Dialog.None,
        val error: Error = Error.None,
    ) : LoadingState()

    enum class Error {
        NoInternet,
        NotEnoughDiskSpace,
        ConsentRequired,
        DownloadFailed,
        DecompressionFailed,
        DatabaseWriteFailed,
        GenericRuntime,
        None
    }

    enum class Dialog {
        CrashConsent,
        Download,
        None
    }
}
