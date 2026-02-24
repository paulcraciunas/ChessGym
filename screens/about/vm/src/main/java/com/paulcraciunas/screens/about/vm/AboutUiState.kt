package com.paulcraciunas.screens.about.vm

data class AboutUiState(
    val libraries: List<LibraryInfo> = emptyList(),
)

data class LibraryInfo(
    val name: String,
    val url: String,
    val license: String,
)
