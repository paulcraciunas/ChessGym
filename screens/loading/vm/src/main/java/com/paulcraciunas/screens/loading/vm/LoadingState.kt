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
        Runtime, // TODO Paul: integrate failure reason into multiple error types
        None
    }

    enum class Dialog {
        Download, // First, we ask for Download confirmation
        Permission, // Next, we ask for notification permission
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

        fun downloadError() = Ready(
            requiresConfirmation = false,
            requiresPermission = false,
            dialog = Dialog.None,
            error = Error.Runtime
        )

        fun downloadAccepted() = Ready(
            requiresConfirmation = false,
            dialog = Dialog.Permission,
        )

        fun permissionDenied() = Ready(
            requiresConfirmation = false,
            error = Error.NoPermission
        )
    }
}
