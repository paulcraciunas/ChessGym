package com.paulcraciunas.screens.loading.vm

sealed class LoadingState {
    data class Downloading(val progress: Progress = Progress.empty()) : LoadingState() {
        data class Progress(
            val download: Int,
            val unpack: Int,
            val buildDb: Int,
        ) {
            companion object {
                fun empty(): Progress =
                    Progress(download = 0, unpack = 0, buildDb = 0)
            }
        }
    }

    data object Complete : LoadingState()
    data class Ready(
        val requiresConfirmation: Boolean = true,
        val requiresPermission: Boolean = true,
        val dialog: Dialog = Dialog.None,
        val error: Error = Error.None,
    ) : LoadingState()

    enum class Error {
        NoInternet,
        NotEnoughDiskSpace,
        NoPermission,
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
        Permission,
        None
    }

    companion object {
        fun ready() = Ready()

        fun error(error: Error) = Ready(
            requiresConfirmation = false,
            requiresPermission = false,
            dialog = Dialog.None,
            error = error
        )

        fun runtimeError(error: Error) = Ready(
            requiresConfirmation = false,
            requiresPermission = false,
            dialog = Dialog.None,
            error = error
        )

        fun downloadAccepted() = Ready(
            requiresConfirmation = false,
            dialog = Dialog.Permission,
        )

        fun permissionDenied() = Ready(
            requiresConfirmation = false,
            error = Error.NoPermission
        )

        fun consentDeclined() = Ready(
            requiresConfirmation = false,
            requiresPermission = false,
            dialog = Dialog.None,
            error = Error.ConsentRequired
        )

        fun consentAccepted() = Ready(
            requiresConfirmation = true,
            dialog = Dialog.Download,
        )
    }
}
