package com.paulcraciunas.screens.settings.vm

import androidx.compose.runtime.Immutable
import com.paulcraciunas.settings.application.api.AppSettings

@Immutable
data class SettingsUiState(
    val isSoundEnabled: Boolean = true,
    val isHapticFeedbackEnabled: Boolean = true,
    val isAutoPromoteEnabled: Boolean = true,
    val isAutoNextPuzzleEnabled: Boolean = false,
    val isShowBordersEnabled: Boolean = true,
    val isHighlightLegalMovesEnabled: Boolean = true,
    val lightMode: AppSettings.LightMode = AppSettings.LightMode.System,
    val language: AppLanguage = AppLanguage.System,
    val isAnimationsEnabled: Boolean = true,
    val isCrashReportingEnabled: Boolean = false,
    val isLoading: Boolean = true,
) {
    enum class AppLanguage(val tag: String) {
        System(""),
        English("en"),
        German("de"),
        Spanish("es"),
        French("fr"),
        Hindi("hi"),
        Indonesian("id"),
        Japanese("ja"),
        Korean("ko"),
        BrazilianPortuguese("pt-BR"),
        Russian("ru"),
        SimplifiedChinese("zh-CN");

        companion object {
            private val tagMap = entries.associateBy { it.tag }

            fun fromTag(tag: String): AppLanguage {
                val baseTag = tag.split("-").firstOrNull() ?: "" // Fallback ; e.g. "en-US" -> "en"

                return when {
                    tag.isBlank() -> System
                    tagMap.containsKey(tag) -> tagMap[tag]!!
                    tagMap.containsKey(baseTag) -> tagMap[baseTag]!!
                    tag == "in" -> Indonesian
                    else -> System
                }
            }
        }
    }
}
